package com.sparta.spartatigers.domain.support.chat.event;

import org.springframework.scheduling.annotation.Async;
import com.sparta.spartatigers.domain.foundation.common.event.ItemLocationUpdatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sparta.spartatigers.domain.support.chat.service.LocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemLocationEventListener {

    private final LocationService locationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleItemLocationUpdated(ItemLocationUpdatedEvent event) {
        try {
            locationService.notifyUsersNearBy(event.getUserId(), event.getAction(), event.getData());
            log.info("[ItemLocationUpdated] 알림 전송 완료 - userId: {}, action: {}", event.getUserId(), event.getAction());
        } catch (Exception e) {
            log.error("[ItemLocationUpdated] 알림 전송 실패 - userId: {}, action: {}, error: {}",
                    event.getUserId(), event.getAction(), e.getMessage(), e);
        }
    }
}
