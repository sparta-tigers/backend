package com.sparta.spartatigers.global.firebase.service;

import com.sparta.spartatigers.global.firebase.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FCMService fcmService;

    public void send(NotificationMessage message) {
        if (message == null || !message.hasValidToken()) {
            return;
        }

        fcmService.sendMessageToToken(
            message.token(),
            message.title(),
            message.body()
        );
    }

    public void sendAfterCommit(NotificationMessage message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendSafely(message);
                    }
                }
            );
        } else {
            sendSafely(message);
        }
    }

    private void sendSafely(NotificationMessage message) {
        try {
            send(message);
        } catch (Exception e) {
            log.warn("[FCM 알림 발송 실패] message={}", message, e);
        }
    }
}
