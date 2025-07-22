package com.handi.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {
//    private final DeviceTokenService deviceTokenService;

    public void sendToUserLatestDevice(String userId, String title, String body) {
        // 토큰 받아오는 로직 구현 필요
//        List<String> tokens = deviceTokenService.getActiveTokens(userId);
//        if (tokens.isEmpty()) {
//            // 토큰 없으면 아무 동작 안 함
//            return;
//        }
//        String token = tokens.get(0);

        
        // Token = 디바이스 고유 ID 느낌
        
        String token = "cMA5DW7GT2WcZ7YkHf6-IJ:APA91bHjeXBnnxq97RbS2wKv3eelpp2fIdIQB7XwRtPHtZHDbfZJgFVn_w8fhnnP0QQlAiHu5gL9tet8Or6X_-RA8kfVH20danOuy_t9uqwDYCrXNgSAbM8";

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "medicine")
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            // 실패 토큰 비활성화
//            deviceTokenService.deactivate(userId, token);

        }
    }
}