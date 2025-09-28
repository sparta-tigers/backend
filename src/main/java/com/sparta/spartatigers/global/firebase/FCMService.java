package com.sparta.spartatigers.global.firebase;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.external.FirebaseException;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public String sendMessageToToken(String token, String title, String body) {
        if (token == null || token.isBlank() || title == null || body == null) {
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }

        Notification notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build();

        Message message = Message.builder()
            .setToken(token)
            .setNotification(notification)
            .build();

        try {
            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메세지 송신에 실패했습니다.: {}", e.getMessage(), e);
            throw new FirebaseException(ExceptionCode.FCM_MESSAGE_NOT_SENT, e);
        }
    }
}
