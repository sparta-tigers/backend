package com.sparta.spartatigers.global.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.external.FirebaseException;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 트랜잭션이 성공적으로 커밋된 후에 알림을 발송합니다.
     * 도메인 서비스에서 트랜잭션 동기화 로직을 직접 다루지 않도록 캡슐화합니다.
     */
    public void sendNotificationAfterCommit(
        String token,
        String title,
        String body,
        Long receiverId
    ) {
        if (token == null || token.isBlank()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        sendMessageToToken(token, title, body);
                    } catch (FirebaseException e) {
                        log.warn(
                            "[FCM 발송 실패] 수신자ID: {}, 사유: {}",
                            receiverId,
                            e.getMessage()
                        );
                    }
                }
            }
        );
    }

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
