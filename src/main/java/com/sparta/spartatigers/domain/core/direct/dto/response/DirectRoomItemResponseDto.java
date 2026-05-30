package com.sparta.spartatigers.domain.core.direct.dto.response;

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

    public static DirectRoomItemResponseDto from(
            Long itemId, String title, String description, String category, String status,
            Long ownerId, String ownerNickname,
            String exchangeStatus, Long opponentId, String opponentNickname) {
        return new DirectRoomItemResponseDto(
                itemId,
                title,
                description,
                category,
                status,
                ownerId,
                ownerNickname,
                exchangeStatus,
                opponentId,
                opponentNickname);
    }
}
