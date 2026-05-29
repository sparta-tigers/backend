package com.sparta.spartatigers.domain.core.direct.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.core.direct.model.DirectMessage;
import com.sparta.spartatigers.domain.core.direct.repository.DirectMessageRepository;

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

    public static DirectRoomMessageResponse from(DirectMessageRepository.MessageProjection projection) {
        return new DirectRoomMessageResponse(
                projection.getMessageId(),
                projection.getSenderId(),
                projection.getSenderNickname(),
                projection.getMessage(),
                projection.getSentAt());
    }
}
