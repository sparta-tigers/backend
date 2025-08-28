package com.sparta.spartatigers.domain.item.dto.response;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.user.dto.UserResponseDto;
import java.time.LocalDateTime;

public record ReadItemResponseDto(
    Long id,
    UserResponseDto user,
    ItemCategory category,
    String title,
    ItemStatus status,
    LocalDateTime createdAt) {

    public static ReadItemResponseDto from(Item item) {

        return new ReadItemResponseDto(
            item.getId(),
            UserResponseDto.from(item.getUser()),
            item.getCategory(),
            item.getTitle(),
            item.getStatus(),
            item.getCreatedAt());
    }
}
