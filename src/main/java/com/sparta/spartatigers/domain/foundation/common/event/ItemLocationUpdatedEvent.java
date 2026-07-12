package com.sparta.spartatigers.domain.foundation.common.event;

import lombok.Getter;

@Getter
public class ItemLocationUpdatedEvent {

    private final Long userId;
    private final String action;
    private final Object data;

    public ItemLocationUpdatedEvent(Long userId, String action, Object data) {
        this.userId = userId;
        this.action = action;
        this.data = data;
    }
}
