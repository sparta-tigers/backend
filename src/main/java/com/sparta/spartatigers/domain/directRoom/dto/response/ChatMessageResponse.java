package com.sparta.spartatigers.domain.directRoom.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private final Long roomId;
    private final Long senderId;
    private final Long messageId;
    private final String senderNickname;
    private final String message;
    private final LocalDateTime sentAt;

    public static ChatMessageResponse from(RedisMessage message) {
        return new ChatMessageResponse(
                message.getRoomId(),
                message.getSenderId(),
                message.getMessageId(),
                message.getSenderNickname(),
                message.getMessage(),
                LocalDateTime.parse(message.getSentAt()));
    }
}
