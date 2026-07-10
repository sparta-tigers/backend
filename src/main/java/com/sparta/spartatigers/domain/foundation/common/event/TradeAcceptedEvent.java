package com.sparta.spartatigers.domain.foundation.common.event;

public record TradeAcceptedEvent(
        Long exchangeRequestId,
        Long requesterId,
        Long responderId,
        Long itemId) {
}
