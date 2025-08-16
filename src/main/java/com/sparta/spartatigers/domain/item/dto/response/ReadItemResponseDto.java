package com.sparta.spartatigers.domain.item.dto.response;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;

public record ReadItemResponseDto(
    Long id,
    Long userId,
    String nickname,
    ItemCategory category,
    String title,
    ItemStatus status
) {

    public static ReadItemResponseDto from(Item item) {

        return new ReadItemResponseDto(item.getId(), item.getUser().getId(), item.getUser().getNickname(), item.getCategory(),
            item.getTitle(), item.getStatus());
    }
}
