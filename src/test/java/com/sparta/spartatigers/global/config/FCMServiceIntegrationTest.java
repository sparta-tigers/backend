package com.sparta.spartatigers.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.FirebaseException;
import com.sparta.spartatigers.global.firebase.FCMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {FCMService.class, FirebaseConfig.class})
class FCMServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FCMServiceIntegrationTest.class);

    @Autowired
    private FCMService fcmService;

    @BeforeEach
    void setUp() {
        log.info("Firebase 인증 상태 확인");
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 FCM 메시지 전송 시, INVALID_ARGUMENT 에러가 발생해야 한다")
    void sendMessage_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "this-is-a-invalid-fcm-token-value";
        String title = "통합 테스트";
        String body = "이 메시지는 전송되지 않아야 합니다.";

        // when & then
        FirebaseException exception = assertThrows(FirebaseException.class, () -> {
            fcmService.sendMessageToToken(invalidToken, title, body);
        });

        assertEquals(ExceptionCode.FCM_MESSAGE_NOT_SENT.getMessage(), exception.getMessage());

        log.info("성공적으로 Firebase로부터 예상된 에러 응답을 받았습니다: {}", exception.getMessage());
    }
}