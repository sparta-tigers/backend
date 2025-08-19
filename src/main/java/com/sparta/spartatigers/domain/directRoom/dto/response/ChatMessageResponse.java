package com.sparta.spartatigers.domain.directRoom.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private LocalDateTime sentAt;

    public static ChatMessageResponse from(RedisMessage message) {
        return new ChatMessageResponse(
                message.getRoomId(),
                message.getSenderId(),
                message.getSenderNickname(),
                message.getMessage(),
                LocalDateTime.parse(message.getSentAt()));
    }
}
