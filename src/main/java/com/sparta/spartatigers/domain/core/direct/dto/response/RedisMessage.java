package com.sparta.spartatigers.domain.core.direct.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.spartatigers.domain.core.direct.model.DirectMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessage {

    private Long senderId;
    private Long roomId;
    private Long messageId;
    private String message;
    private String sentAt;
    private String senderNickname;
    @JsonProperty("read")
    private boolean isRead;

    public static RedisMessage from(DirectMessage message) {
        return new RedisMessage(
                message.getSender().getId(),
                message.getDirectRoom().getId(),
                message.getId(),
                message.getMessage(),
                message.getSentAt().toString(),
                message.getSender().getNickname(),
                message.isRead()
        );
    }

    public static RedisMessage readStatus(Long messageId, Long roomId, boolean isRead) {
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.messageId = messageId;
        redisMessage.roomId = roomId;
        redisMessage.isRead = isRead;
        redisMessage.sentAt = LocalDateTime.now().toString();
        return redisMessage;
    }
}
