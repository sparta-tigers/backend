package com.sparta.spartatigers.domain.core.trade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRoomResponseDto {

    private Long directRoomId;
    private Long exchangeRequestId;

    public static ExchangeRoomResponseDto created(Long exchangeRequestId) {
        return new ExchangeRoomResponseDto(null, exchangeRequestId);
    }

    public static ExchangeRoomResponseDto rejected(Long exchangeRequestId) {
        return new ExchangeRoomResponseDto(null, exchangeRequestId);
    }
}
