package com.sparta.spartatigers.domain.core.trade.dto.response;

import com.sparta.spartatigers.domain.core.trade.model.ExchangeRequest;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;
import com.sparta.spartatigers.domain.core.trade.model.ItemCategory;
import com.sparta.spartatigers.domain.core.trade.model.ItemStatus;
import com.sparta.spartatigers.domain.foundation.user.account.dto.UserResponseDto;
import java.time.LocalDateTime;

public record SendRequestResponseDto(
    Long exchangeRequestId,
    Long itemId,
    UserResponseDto receiver,
    ItemCategory category,
    String title,
    ItemStatus status,
    ExchangeStatus exchangeStatus,
    LocalDateTime createdAt,
    Long directRoomId
) {

    public static SendRequestResponseDto from(ExchangeRequest exchangeRequest, Long directRoomId) {

        return new SendRequestResponseDto(
            exchangeRequest.getId(),
            exchangeRequest.getItem().getId(),
            UserResponseDto.from(exchangeRequest.getReceiver()),
            exchangeRequest.getItem().getCategory(),
            exchangeRequest.getItem().getTitle(),
            exchangeRequest.getItem().getStatus(),
            exchangeRequest.getStatus(),
            exchangeRequest.getCreatedAt(),
            directRoomId
        );
    }
}
