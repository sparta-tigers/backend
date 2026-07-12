package com.sparta.spartatigers.domain.foundation.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemStatusChangedEvent {

    private final Long exchangeRequestId;
    private final String message;
}
