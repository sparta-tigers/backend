package com.sparta.spartatigers.domain.core.trade.dto.response;

import com.sparta.spartatigers.domain.core.trade.model.Item;
import com.sparta.spartatigers.domain.core.trade.model.ItemCategory;
import com.sparta.spartatigers.domain.core.trade.model.ItemStatus;

public record ItemResponseDto(
    Long id,
    ItemCategory category,
    String title,
    ItemStatus status
) {
    public static ItemResponseDto from(Item item) {
        return new ItemResponseDto(
            item.getId(),
            item.getCategory(),
            item.getTitle(),
            item.getStatus()
        );
    }
}
