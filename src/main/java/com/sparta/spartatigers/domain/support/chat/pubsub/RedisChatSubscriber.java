package com.sparta.spartatigers.domain.support.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.support.chat.model.ChatMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatMessage chatMessage = objectMapper.readValue(
                json,
                ChatMessage.class
            );

            // 전송 경로 분기
            String path = switch (chatMessage.getDomain()) {
                case EXCHANGE -> "/server/directRoom/" +
                chatMessage.getRoomId();
                case LIVEBOARD -> "/server/liveboard/room/" +
                chatMessage.getRoomId();
                default -> null;
            };

            // 메세지 전송
            messagingTemplate.convertAndSend(path, chatMessage);
        } catch (Exception e) {
            throw new RuntimeException("RedisChatSubscriber 오류 발생", e);
        }
    }
}
