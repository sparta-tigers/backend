package com.sparta.spartatigers.domain.directRoom.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.directRoom.model.DirectMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectRoomMessageResponse {

    private final Long messageId;
    private final Long senderId;
    private final String senderNickname;
    private final String message;
    private final LocalDateTime sentAt;

    public static DirectRoomMessageResponse from(DirectMessage message) {
        return new DirectRoomMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getSentAt());
    }
}
