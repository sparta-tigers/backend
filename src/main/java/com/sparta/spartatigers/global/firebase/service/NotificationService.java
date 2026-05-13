package com.sparta.spartatigers.global.firebase.service;

import com.sparta.spartatigers.global.firebase.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FCMService fcmService;

    public void send(NotificationMessage message) {

        if (!message.hasValidToken()) {
            return;
        }

        fcmService.sendMessageToToken(message.token(), message.title(), message.body());
    }
}
