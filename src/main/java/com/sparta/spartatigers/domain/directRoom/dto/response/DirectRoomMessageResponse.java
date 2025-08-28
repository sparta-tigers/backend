package com.sparta.spartatigers.domain.directRoom.dto.response;

import com.sparta.spartatigers.domain.directRoom.model.DirectMessage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectRoomMessageResponse {

    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private LocalDateTime sentAt;

    public static DirectRoomMessageResponse from(DirectMessage message) {
        return new DirectRoomMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getSentAt());
    }
}
