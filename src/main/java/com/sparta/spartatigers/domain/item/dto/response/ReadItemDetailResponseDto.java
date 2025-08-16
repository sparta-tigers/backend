package com.sparta.spartatigers.domain.item.dto.response;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import java.time.LocalDateTime;

public record ReadItemDetailResponseDto(
    Long id,
    Long userId,
    String nickname,
    ItemCategory category,
    String image,
    String seatInfo,
    String title,
    String description,
    ItemStatus status,
    LocalDateTime createdAt) {

    public static ReadItemDetailResponseDto from(Item item) {

        return new ReadItemDetailResponseDto(
            item.getId(),
            item.getUser().getId(),
            item.getUser().getNickname(),
            item.getCategory(),
            item.getImage(),
            item.getSeatInfo(),
            item.getTitle(),
            item.getDescription(),
            item.getStatus(),
            item.getCreatedAt());
    }
}
