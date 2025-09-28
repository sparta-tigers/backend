package com.sparta.spartatigers.domain.exchangerequest.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.user.dto.UserResponseDto;

public record ReceiveRequestResponseDto(
    Long exchangeRequestId,
    Long itemId,
    UserResponseDto sender,
    ItemCategory category,
    String title,
    ItemStatus status,
    LocalDateTime createdAt) {

    public static ReceiveRequestResponseDto from(ExchangeRequest exchangeRequest) {

        return new ReceiveRequestResponseDto(
            exchangeRequest.getId(),
            exchangeRequest.getItem().getId(),
            UserResponseDto.from(exchangeRequest.getSender()),
            exchangeRequest.getItem().getCategory(),
            exchangeRequest.getItem().getTitle(),
            exchangeRequest.getItem().getStatus(),
            exchangeRequest.getCreatedAt());
    }
}
