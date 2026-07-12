package com.sparta.spartatigers.domain.core.direct.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.core.direct.dto.response.ChatMessageResponse;
import com.sparta.spartatigers.domain.core.direct.dto.response.RedisMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDirectMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisMessage payload = objectMapper.readValue(
                body,
                RedisMessage.class
            );

            ChatMessageResponse response = ChatMessageResponse.from(payload);

            messagingTemplate.convertAndSend(
                "/server/directRoom/" + payload.getRoomId(),
                response
            );
            log.info("Redis 메시지 수신 처리 완료");
        } catch (Exception e) {
            log.error("RedisDirectMessageSubscriber 오류 발생", e);
        }
    }
}
