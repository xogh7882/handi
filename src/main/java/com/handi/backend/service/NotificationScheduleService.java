package com.handi.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handi.backend.entity.*;
import com.handi.backend.enums.MedicationSchedule;
import com.handi.backend.enums.Role;
import com.handi.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationScheduleService {
    private final OrganizationsRepository organizationsRepository;
    private final UsersRepository usersRepository;
    private final SeniorUserRelationsRepository seniorUserRelationsRepository;
    private final MedicationSchedulesRepository medicationSchedulesRepository;
    private final MedicationsRepository medicationsRepository;
    private final SeniorsRepository seniorsRepository;
    private final Set<String> sentNotifications = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedRate = 900000) // 15분 = 900,000 ms
    public void checkAndSendNotifications() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        List<Users> employees = usersRepository.findAll().stream()
                .filter(user -> Role.EMPLOYEE.equals(user.getRole()))
                .toList();

        for (Users employee : employees) {
            checkEmployeeMedicationNotifications(employee, now, today);
        }
    }


    private void checkEmployeeMedicationNotifications(Users employee, LocalTime now, LocalDate today) {

        // employee 담당 환자 리스트 조회
        List<Integer> seniorIds = seniorUserRelationsRepository.findSeniorIdsByUserId(employee.getId());

        for (Integer seniorId : seniorIds) {
            Seniors senior = seniorsRepository.findById(seniorId).orElse(null);
            if (senior == null) continue;

            // 해당 환자의 투약 일정 조회 (현재 날짜가 기간 내에 있는 것만)
            List<MedicationSchedules> activeSchedules = medicationSchedulesRepository.findAll().stream()
                    .filter(schedule -> schedule.getSenior().getId().equals(seniorId))
                    .filter(schedule -> isDateInRange(today, schedule.getMedicationStartdate(), schedule.getMedicationEnddate()))
                    .toList();

            for (MedicationSchedules schedule : activeSchedules) {
                checkMedicationTimings(employee, senior, schedule, now, today);
            }
        }
    }

    private void checkMedicationTimings(Users employee, Seniors senior, MedicationSchedules schedule, LocalTime now, LocalDate today) {

        // 해당 환자의 소속 기관 정보 조회
        Organizations organization = organizationsRepository.findById(Integer.parseInt(employee.getOrganizationId())).orElse(null);
        if (organization == null) return;

        // 각 투약 시간대별로 확인
        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.BEFORE_BREAKFAST,
                organization.getBreakfastTime(), "아침 식전", now, today, -30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.AFTER_BREAKFAST,
                organization.getBreakfastTime(), "아침 식후", now, today, 30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.BEFORE_LUNCH,
                organization.getLunchTime(), "점심 식전", now, today, -30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.AFTER_LUNCH,
                organization.getLunchTime(), "점심 식후", now, today, 30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.BEFORE_DINNER,
                organization.getDinnerTime(), "저녁 식전", now, today, -30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.AFTER_DINNER,
                organization.getDinnerTime(), "저녁 식후", now, today, 30);

        checkSpecificMedicationTiming(employee, senior, schedule, MedicationSchedule.BEDTIME,
                organization.getSleepTime(), "취침 전", now, today, -30);
    }

    private void checkSpecificMedicationTiming(Users employee, Seniors senior, MedicationSchedules schedule,
                                               MedicationSchedule medicationSchedule, LocalTime mealTime,
                                               String timingDescription, LocalTime now, LocalDate today, int minuteOffset) {

        if (mealTime == null) return;

        // 해당 투약 시간대에 투약이 있는지 확인
        List<Medications> medications = medicationsRepository.findBySeniorIdAndSchedule(senior.getId(), medicationSchedule);
        if (medications.isEmpty()) return;

        // 알림 시간 계산
        LocalTime notificationTime = mealTime.plusMinutes(minuteOffset);

        // 현재 시간이 알림 시간과 1분 이내에 있는지 확인
        if (!shouldSendNotification(notificationTime, now)) return;

        // 중복 알림 방지를 위한 키 생성
        String notificationKey = employee.getId() + "_" + senior.getId() + "_" + medicationSchedule.name() + "_" + today;

        if (sentNotifications.contains(notificationKey)) return;

        // FCM 토큰이 있는 경우에만 알림 전송
        if (employee.getFcmToken() != null && !employee.getFcmToken().trim().isEmpty()) {
            String title = "투약 알림";
            String message = String.format("%s님에게 %s 약을 투여해야 합니다. (%s)",
                    senior.getName(), timingDescription, schedule.getMedicationName());

            try {
                sendFcmNotification(employee.getFcmToken(), title, message, senior);
                System.out.println("투약 알림 전송 성공: " + employee.getName() + " -> " + message);

                // 중복 방지를 위해 기록
                sentNotifications.add(notificationKey);

            } catch (Exception e) {
                System.err.println("FCM 알림 전송 실패: " + employee.getName() + " - " + e.getMessage());
            }
        }
    }

    private boolean shouldSendNotification(LocalTime notificationTime, LocalTime now) {
        // 현재 시간이 알림 시간과 1분 이내에 있는지 확인
        return Math.abs(now.toSecondOfDay() - notificationTime.toSecondOfDay()) <= 60;
    }



    private boolean isDateInRange(LocalDate currentDate, String startDateStr, String endDateStr) {
        try {
            if (startDateStr == null || endDateStr == null) return false;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
        } catch (Exception e) {
            System.err.println("날짜 파싱 오류: " + e.getMessage());
            return false;
        }
    }

    private void sendFcmNotification(String fcmToken, String title, String message, Seniors senior) {
        // FcmService의 기존 메서드를 FCM 토큰으로 직접 호출하도록 수정이 필요
        // 여기서는 직접 FCM 메시지를 보내는 방식으로 구현
        try {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("seniorId", senior.getId());
            String dataJson = mapper.writeValueAsString(dataMap);

            com.google.firebase.messaging.Message fcmMessage = com.google.firebase.messaging.Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .putData("type", "medicine")
                    .putData("data", dataJson)
                    .build();

            com.google.firebase.messaging.FirebaseMessaging.getInstance().send(fcmMessage);
        } catch (Exception e) {
            throw new RuntimeException("FCM 메시지 전송 실패", e);
        }
    }

    // 매일 자정에 전송된 알림 기록 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyNotifications() {
        sentNotifications.clear();
        System.out.println("🔄 일일 알림 기록 초기화 완료");
    }
}
