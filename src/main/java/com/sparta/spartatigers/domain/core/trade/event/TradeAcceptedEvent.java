package com.sparta.spartatigers.domain.core.trade.event;

public record TradeAcceptedEvent(
    Long exchangeRequestId,
    Long requesterId,
    Long responderId,
    Long itemId
) {}
