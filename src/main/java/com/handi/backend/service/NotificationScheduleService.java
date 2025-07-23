package com.handi.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handi.backend.entity.*;
import com.handi.backend.enums.ConsultationStatus;
import com.handi.backend.enums.MedicationSchedule;
import com.handi.backend.enums.Role;
import com.handi.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final AutoScheduleMatchesRepository autoScheduleMatchesRepository;
    private final ConsultationsRepository consultationsRepository;
    private final Set<String> sentNotifications = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedRate = 600000) // 10분 = 600,000 ms
    public void checkAndSendNotifications() {
        LocalTime now = LocalTime.now();     // 현재시간
        LocalDate today = LocalDate.now();   // 현재날짜
        LocalDateTime nowDateTime = LocalDateTime.now();  //현재 날짜+시간

        List<Users> allUsers = usersRepository.findAll();   // 전체 유저 가져오기

        for (Users user : allUsers) {
            // 간호사: 투약 알림 + 상담 알림
            if (Role.EMPLOYEE.equals(user.getRole())) {
                checkMedicationNotifications(user, now, today);
                checkConsultationNotifications(user, nowDateTime, today);
            }

            // 보호자: 상담 알림
            if (Role.GUARDIAN.equals(user.getRole())) {
                checkConsultationNotifications(user, nowDateTime, today);
            }
        }
    }

    // 투약 알림 체크
    private void checkMedicationNotifications(Users employee, LocalTime now, LocalDate today) {
        // 담당 환자 리스트 조회
        List<Integer> seniorIds = seniorUserRelationsRepository.findSeniorIdsByUserId(employee.getId());

        for (Integer seniorId : seniorIds) {
            Seniors senior = seniorsRepository.findById(seniorId).orElse(null);
            if (senior == null) continue;

            // 해당 환자의 투약 일정 조회
            List<MedicationSchedules> activeSchedules = medicationSchedulesRepository.findAll().stream()
                    .filter(schedule -> schedule.getSenior().getId().equals(seniorId))  // id가 같고
                    .filter(schedule -> isDateInRange(today, schedule.getMedicationStartdate(), schedule.getMedicationEnddate()))  // 날짜 안에 속하고
                    .toList();


            // 해당 투약 스케줄 중 시간 확인하기
            for (MedicationSchedules schedule : activeSchedules) {
                checkMedicationTimings(employee, senior, schedule, now, today);
            }
        }
    }

    // 상담 알림 (간호사/보호자 공통)
    private void checkConsultationNotifications(Users user, LocalDateTime nowDateTime, LocalDate today) {
        List<AutoScheduleMatches> relevantMatches;
        String userType;

        if (Role.EMPLOYEE.equals(user.getRole())) {
            // 간호사 상담 조회
            relevantMatches = autoScheduleMatchesRepository.findAll().stream()
                    .filter(match -> match.getUser().getId().equals(user.getId()))         // 스케줄이 나의 스케줄이고
                    .filter(match -> match.getMeetingDate() != null)                       // 날짜가 null이 아니고
                    .filter(match -> match.getMeetingDate().toLocalDate().equals(today))   // 날짜가 오늘이고
                    .filter(match -> match.getMeetingDate().isAfter(nowDateTime))          // 상담 시간이 현재 이후고
                    .toList();
            userType = "간호사";

        } else if (Role.GUARDIAN.equals(user.getRole())) {
            // 보호자 상담 조회
            List<Integer> seniorIds = seniorUserRelationsRepository.findSeniorIdsByUserId(user.getId());
            relevantMatches = autoScheduleMatchesRepository.findAll().stream()
                    .filter(match -> seniorIds.contains(match.getSenior().getId()))
                    .filter(match -> match.getMeetingDate() != null)
                    .filter(match -> match.getMeetingDate().toLocalDate().equals(today))
                    .filter(match -> match.getMeetingDate().isAfter(nowDateTime))
                    .toList();
            userType = "보호자";

        } else {
            return; // 다른 역할 return
        }

        // 상담 매칭별로 알림 처리 ( PENDING 상태만 알람 보내기 )
        for (AutoScheduleMatches match : relevantMatches) {
            // 해당 매칭에 대한 상담이 있고 PENDING 상태인지 확인
            boolean hasPendingConsultation = consultationsRepository.findAll().stream()
                    .anyMatch(consultation ->
                            consultation.getAutoScheduleMatch().getId().equals(match.getId()) &&
                                    ConsultationStatus.PENDING.equals(consultation.getStatus())
                    );

            if (hasPendingConsultation) {
                Seniors senior = match.getSenior();
                LocalDateTime consultationTime = match.getMeetingDate();

                // 상담 30분 전 알림
                checkConsultationTiming(user, senior, consultationTime, nowDateTime, today, 30, userType, match);

                // 상담 10분 전 알림
                checkConsultationTiming(user, senior, consultationTime, nowDateTime, today, 10, userType, match);
            }
        }
    }

    // 상담 알림 시간 체크 및 전송
    private void checkConsultationTiming(Users user, Seniors senior, LocalDateTime consultationTime,
                                         LocalDateTime nowDateTime, LocalDate today, int minutesBefore, String userType, AutoScheduleMatches match) {

        // 알림 시간 계산 (상담 시간에서 지정된 분만큼 이전)
        LocalDateTime notificationTime = consultationTime.minusMinutes(minutesBefore);

        // 중복 알림 방지를 위한 키 생성
        String notificationKey = user.getId() + "_" + senior.getId() + "_consultation_" + minutesBefore + "_" + today;

        if (sentNotifications.contains(notificationKey)) return;      // 이미 알림을 보낸 경우 return

        // FCM 토큰이 있는 경우에만 알림 전송
        if (user.getFcmToken() != null && !user.getFcmToken().trim().isEmpty()) {
            String title = "상담 알림";
            String message = String.format("%s환자 상담 %d분 전입니다.", senior.getName(), minutesBefore);

            try {
                sendConsultationFcmNotification(user.getFcmToken(), title, message, senior, match.getId());

                // 중복 방지를 위해 기록
                sentNotifications.add(notificationKey);

            } catch (Exception e) {
                System.err.println(String.format("FCM 상담 알림 전송 실패 (%s): %s - %s",
                        userType, user.getName(), e.getMessage()));
            }
        }
    }


    // 투약 알림 시간 찾기
    private void checkMedicationTimings(Users employee, Seniors senior, MedicationSchedules schedule, LocalTime now, LocalDate today) {
        // 해당 환자의 소속 기관 정보 조회 ( 식사 시간 맞추기 )
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

    private void checkSpecificMedicationTiming(Users employee, Seniors senior, MedicationSchedules schedule, MedicationSchedule medicationSchedule,
                                               LocalTime mealTime, String timingDescription, LocalTime now, LocalDate today, int minuteOffset) {

        if (mealTime == null) return;

        // 해당 투약 시간대에 투약이 있는지 확인
        List<Medications> medications = medicationsRepository.findBySeniorIdAndSchedule(senior.getId(), medicationSchedule);
        if (medications.isEmpty()) return;

        // 알림 시간 계산
        LocalTime notificationTime = mealTime.plusMinutes(minuteOffset);

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



    // 날짜가 원하는 날짜 안에 속하는가 ( start <= current <= end )
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

    // FCM 알림 전송 - 상담용
    private void sendConsultationFcmNotification(String fcmToken, String title, String message, Seniors senior, Integer autoScheduleMatchId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("SeniorId", senior.getId());
            dataMap.put("AutoScheduleMatchId", autoScheduleMatchId);

            String dataJson = mapper.writeValueAsString(dataMap);

            com.google.firebase.messaging.Message fcmMessage = com.google.firebase.messaging.Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .putData("type", "consultation")
                    .putData("data", dataJson)
                    .build();

            com.google.firebase.messaging.FirebaseMessaging.getInstance().send(fcmMessage);
        } catch (Exception e) {
            throw new RuntimeException("FCM 상담 알림 전송 실패", e);
        }
    }

    // FCM 알림 전송 - 투약용
    private void sendFcmNotification(String fcmToken, String title, String message, Seniors senior) {
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


