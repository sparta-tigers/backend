package com.sparta.spartatigers.domain.directRoom.dto.response;

import com.sparta.spartatigers.domain.item.model.Item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DirectRoomItemResponseDto {

    private Long itemId;
    private String title;
    private String description;
    private String category;
    private String status;
    private Long ownerId;
    private String ownerNickname;
    private String exchangeStatus;
    private Long opponentId;
    private String opponentNickname;

    public static DirectRoomItemResponseDto from(Item item, String exchangeStatus, Long opponentId, String opponentNickname) {
        return new DirectRoomItemResponseDto(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getCategory().name(),
            item.getStatus().name(),
            item.getUser().getId(),
            item.getUser().getNickname(),
            exchangeStatus,
            opponentId,
            opponentNickname);
    }
}
