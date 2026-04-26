package com.sparta.spartatigers.domain.exchangerequest.dto.response;

import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomCreateResponseDto;

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

    public static ExchangeRoomResponseDto from(DirectRoomCreateResponseDto directRoom) {
        return new ExchangeRoomResponseDto(directRoom.getDirectRoomId(), directRoom.getExchangeRequestId());
    }

    public static ExchangeRoomResponseDto rejected(Long exchangeRequestId) {
        return new ExchangeRoomResponseDto(null, exchangeRequestId);
    }
}
