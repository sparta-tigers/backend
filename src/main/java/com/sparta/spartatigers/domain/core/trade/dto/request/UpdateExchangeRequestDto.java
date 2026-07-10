package com.sparta.spartatigers.domain.core.trade.dto.request;

import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateExchangeRequestDto(
                @NotNull(message = "변경할 상태값은 필수입니다.") ExchangeStatus status) {
}
