package com.sparta.spartatigers.domain.core.direct.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.core.direct.model.DirectRoom;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeRequest;
import com.sparta.spartatigers.domain.core.trade.model.Item;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DirectRoomResponseDto {

    private Long directRoomId;
    private Long unreadCount;
    private Long exchangeRequestId;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private boolean isCompleted;
    private String itemTitle;
    private String itemCategory;
    private String itemImage;
    private String opponentNickname;
    private String opponentImage;
    private boolean opponentOnline;

    public static DirectRoomResponseDto from(
            DirectRoom room, ExchangeRequest exchangeRequest, Long unreadCount, Long currentUserId, boolean opponentOnline) {
        Item item = exchangeRequest.getItem();

        boolean isSender = room.getSender().getId().equals(currentUserId);
        User opponent = isSender ? room.getReceiver() : room.getSender();

        return new DirectRoomResponseDto(
                room.getId(),
                unreadCount,
                exchangeRequest.getId(),
                room.getSender().getId(),
                room.getReceiver().getId(),
                room.getCompletedAt(),
                room.getCreatedAt(),
                room.isCompleted(),
                item.getTitle(),
                item.getCategory().name(),
                item.getImage(),
                opponent.getNickname(),
                opponent.getProfileImageUrl(),
                opponentOnline);
    }
}
