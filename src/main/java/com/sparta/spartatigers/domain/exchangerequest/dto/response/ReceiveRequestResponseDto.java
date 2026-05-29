package com.sparta.spartatigers.domain.exchangerequest.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeStatus;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.foundation.user.account.dto.UserResponseDto;

/**
 * 교환 요청 목록 응답 DTO
 *
 * <p>status: 아이템 자체의 상태 (ItemStatus) — REGISTERED, COMPLETED 등
 * <p>exchangeStatus: 교환 요청의 진행 상태 (ExchangeStatus) — PENDING, ACCEPTED, REJECTED, COMPLETED
 * <p>이 두 필드는 다른 개념이므로 반드시 구분하여 사용해야 합니다.
 * <p>기존에는 exchangeStatus가 없어서 프론트엔드에서 요청 수락/거절 여부를 알 수 없었습니다.
 */
public record ReceiveRequestResponseDto(
    Long exchangeRequestId,
    Long itemId,
    UserResponseDto sender,
    ItemCategory category,
    String title,
    ItemStatus status,
    ExchangeStatus exchangeStatus,
    LocalDateTime createdAt,
    Long directRoomId) {

    public static ReceiveRequestResponseDto from(ExchangeRequest exchangeRequest, Long directRoomId) {

        return new ReceiveRequestResponseDto(
            exchangeRequest.getId(),
            exchangeRequest.getItem().getId(),
            UserResponseDto.from(exchangeRequest.getSender()),
            exchangeRequest.getItem().getCategory(),
            exchangeRequest.getItem().getTitle(),
            exchangeRequest.getItem().getStatus(),
            exchangeRequest.getStatus(),
            exchangeRequest.getCreatedAt(),
            directRoomId);
    }
}
