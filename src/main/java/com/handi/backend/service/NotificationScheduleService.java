package com.handi.backend.service;

import com.handi.backend.entity.Medications;
import com.handi.backend.entity.Organizations;
import com.handi.backend.entity.Users;
import com.handi.backend.enums.MedicationSchedule;
import com.handi.backend.repository.MedicationsRepository;
import com.handi.backend.repository.OrganizationsRepository;
import com.handi.backend.repository.SeniorUserRelationsRepository;
import com.handi.backend.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationScheduleService {
    private final OrganizationsRepository organizationsRepository;
    private final UsersRepository usersRepository;
    private final SeniorUserRelationsRepository seniorUserRelationsRepository;
    private final MedicationsRepository medicationsRepository;
    private final FcmService fcmService;

    private final Set<String> sentNotifications = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedRate = 1200000) // 20분 = 1,200,000 ms
    public void checkAndSendNotifications() {
        LocalTime now = LocalTime.now();

        List<Organizations> organizations = organizationsRepository.findAll();

        for (Organizations org : organizations) {
            checkMealNotifications(org, now);
        }
    }

    private void checkMealNotifications(Organizations org, LocalTime now) {
        String today = java.time.LocalDate.now().toString();

        // 아침식사 30분 전
        if (shouldSendNotification(org.getBreakfastTime(), now)) {
            String notificationKey = org.getId() + "_breakfast_" + today;

            // 중복방지 ( 이미 알림을 보냈을 경우 안보내도록 해야함 )
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "아침식사", "아침 식사 30분 전 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.BEFORE_BREAKFAST, "아침 식전 투약");

                // 중복 관리를 위해 추가
                sentNotifications.add(notificationKey);
            }
        }

        // 아침식사 30분 후
        if (shouldSendNotification(org.getBreakfastTime().plusMinutes(30), now)) {
            String notificationKey = org.getId() + "_after_breakfast_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "아침식사", "아침 식사 30분 후 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.AFTER_BREAKFAST, "아침 식후 투약");
                sentNotifications.add(notificationKey);
            }
        }


        // 점심식사 30분 전
        if (shouldSendNotification(org.getLunchTime(), now)) {
            String notificationKey = org.getId() + "_lunch_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "점심식사", "점심 식사 30분 전 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.BEFORE_LUNCH, "점심 식전 투약");
                sentNotifications.add(notificationKey);
            }
        }

        // 점심식사 30분 후
        if (shouldSendNotification(org.getLunchTime().plusMinutes(30), now)) {
            String notificationKey = org.getId() + "_after_lunch_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "점심식사", "점심 식사 30분 후 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.AFTER_LUNCH, "점심 식후 투약");
                sentNotifications.add(notificationKey);
            }
        }

        // 저녁식사 30분 전
        if (shouldSendNotification(org.getDinnerTime(), now)) {
            String notificationKey = org.getId() + "_dinner_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "저녁식사", "저녁 식사 30분 전 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.BEFORE_DINNER, "저녁 식전 투약");
                sentNotifications.add(notificationKey);
            }
        }

        // 저녁식사 30분 후
        if (shouldSendNotification(org.getDinnerTime().plusMinutes(30), now)) {
            String notificationKey = org.getId() + "_after_dinner_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "저녁식사", "저녁 식사 30분 후 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.AFTER_DINNER, "저녁 식후 투약");
                sentNotifications.add(notificationKey);
            }
        }

        // 취침 30분 전
        if (shouldSendNotification(org.getSleepTime(), now)) {
            String notificationKey = org.getId() + "_sleep_" + today;
            if (!sentNotifications.contains(notificationKey)) {
                sendMealNotification(org, "취침시간", "취침 30분 전 입니다. 약을 투약해주세요");
                checkMedicationNotification(org, MedicationSchedule.BEDTIME, "취침 전 투약");
                sentNotifications.add(notificationKey);
            }
        }
    }

    private boolean shouldSendNotification(LocalTime mealTime, LocalTime now) {
        if (mealTime == null) return false;

        // 30분 전 시간 계산
        LocalTime notificationTime = mealTime.minusMinutes(30);

        // 현재 시간이 알림 시간과 1분 이내에 있는지 확인
        return Math.abs(now.toSecondOfDay() - notificationTime.toSecondOfDay()) <= 60;
    }

    private void sendMealNotification(Organizations org, String mealType, String message) {
        try {
            List<Users> users = usersRepository.findAll().stream()
                    .filter(user -> org.getId().toString().equals(user.getOrganizationId()))
                    .toList();

            String title = org.getName() + " " + mealType + " 알림";

            for (Users user : users) {
                fcmService.sendToUserLatestDevice(user.getId().toString(), title, message);
                System.out.println("식사 알림 전송: " + user.getName() + " - " + title + ": " + message);
            }

        } catch (Exception e) {
            System.err.println("식사 알림 전송 실패: " + e.getMessage());
        }
    }

    private void checkMedicationNotification(Organizations org, MedicationSchedule scheduleType, String medicationType) {
        try {
            List<Users> users = usersRepository.findAll().stream()
                    .filter(user -> org.getId().toString().equals(user.getOrganizationId()))
                    .toList();

            for (Users user : users) {
                List<Integer> seniorIds = seniorUserRelationsRepository.findSeniorIdsByUserId(user.getId());

                for (Integer seniorId : seniorIds) {
                    List<Medications> medications = medicationsRepository.findBySeniorIdAndSchedule(seniorId, scheduleType);

                    if (!medications.isEmpty()) {
                        String title = medicationType + " 알림";
                        String message = medications.size() + "개의 투약이 예정되어 있습니다.";

                        fcmService.sendToUserLatestDevice(user.getId().toString(), title, message);
                        System.out.println("투약 알림 전송: " + user.getName() + " - " + title + ": " + message);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("투약 알림 전송 실패: " + e.getMessage());
        }
    }

    // 매일 자정에 전송된 알림 기록 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyNotifications() {
        sentNotifications.clear();
        System.out.println("🔄 일일 알림 기록 초기화 완료");
    }
}
