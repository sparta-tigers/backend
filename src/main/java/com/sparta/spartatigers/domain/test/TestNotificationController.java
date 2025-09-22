package com.sparta.spartatigers.domain.test;

import com.sparta.spartatigers.global.firebase.FCMService;
import com.sparta.spartatigers.global.notification.NotificationSender;
import com.sparta.spartatigers.global.notification.dto.AlertLevel;
import com.sparta.spartatigers.global.notification.dto.MessagePayload;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestNotificationController {

    private final NotificationSender notificationSender;
    private final FCMService fcmService;

    @GetMapping("/test/notification")
    public String testNotification() {
        MessagePayload payload = MessagePayload.builder()
            .level(AlertLevel.CRITICAL)
            .subject("🔔 테스트 알림 입니다.")
            .message("이 메시지가 보인다면 디스코드 알림 기능이 정상적으로 동작하는 것입니다.")
            .metadata(Map.of(
                "Test Initiator", "hyun2y00",
                "Timestamp", LocalDateTime.now().toString()
            ))
            .backlink("https://github.com/yaguniv-server/be-sparta-tigers")
            .build();

        notificationSender.send(payload);

        return "Discord test notification has been sent!";
    }

    @GetMapping("/test/firebase-exception")
    public String testFirebaseException() {
        String invalidToken = "this-is-an-invalid-token-that-will-cause-an-error";
        String testTitle = "Firebase 에러 테스트";
        String testBody = "이 메시지는 발송되면 안됩니다.";

        fcmService.sendMessageToToken(invalidToken, testTitle, testBody);

        return "This message should not be returned.";
    }
}
