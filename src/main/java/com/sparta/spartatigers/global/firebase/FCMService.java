package com.sparta.spartatigers.global.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FCMService {

    public String sendMessageToToken(String token, String title, String body) {
        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .setToken(token)
            .build();

        try {
            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("Successfully sent message: " + response);
            return response;
        } catch (FirebaseMessagingException e) {
//            System.err.println("Failed to send message: " + e.getMessage());
            return "Error sending message";
        }
    }
}
