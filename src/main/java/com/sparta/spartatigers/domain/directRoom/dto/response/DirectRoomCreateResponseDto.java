package com.sparta.spartatigers.domain.directRoom.dto.response;

import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DirectRoomCreateResponseDto {

    private Long directRoomId;
    private Long exchangeRequestId;
    private Long senderId;
    private Long receiverId;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static DirectRoomCreateResponseDto from(DirectRoom room) {
        return new DirectRoomCreateResponseDto(
                room.getId(),
                room.getExchangeRequest().getId(),
                room.getSender().getId(),
                room.getReceiver().getId(),
                room.isCompleted(),
                room.getCompletedAt(),
                room.getCreatedAt());
    }
}
