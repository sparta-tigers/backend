package com.sparta.spartatigers.domain.item.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.user.dto.UserResponseDto;

public record ReadItemDetailResponseDto(
    Long id,
    UserResponseDto user,
    ItemCategory category,
    String image,
    String seatInfo,
    String title,
    String description,
    ItemStatus status,
    LocalDateTime createdAt,
    Integer distance) {

    public static ReadItemDetailResponseDto from(Item item, Integer distance) {

        return new ReadItemDetailResponseDto(
            item.getId(),
            UserResponseDto.from(item.getUser()),
            item.getCategory(),
            item.getImage(),
            item.getSeatInfo(),
            item.getTitle(),
            item.getDescription(),
            item.getStatus(),
            item.getCreatedAt(),
            distance);
    }
}
