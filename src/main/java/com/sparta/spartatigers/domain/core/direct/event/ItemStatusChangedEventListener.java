package com.sparta.spartatigers.domain.core.direct.event;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sparta.spartatigers.domain.core.direct.dto.response.RedisMessage;
import com.sparta.spartatigers.domain.core.direct.pubsub.RedisDirectMessagePublisher;
import com.sparta.spartatigers.domain.core.direct.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.foundation.common.event.ItemStatusChangedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemStatusChangedEventListener {

    private final DirectRoomRepository directRoomRepository;
    private final RedisDirectMessagePublisher redisDirectMessagePublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleItemStatusChangedEvent(ItemStatusChangedEvent event) {
        log.info("[ItemStatusChangedEventListener] 거래 상태 변경 이벤트 수신 - exchangeRequestId: {}", event.getExchangeRequestId());

        directRoomRepository.findByExchangeRequestId(event.getExchangeRequestId())
                .ifPresent(room -> {
                    if (event.getMessage().equals("거래가 완료되었습니다.")) {
                        room.complete();
                    }
                    
                    Map<String, Object> payload = Map.of(
                            "type", "SYSTEM",
                            "action", "STATUS_UPDATED",
                            "roomId", room.getId());
                    redisDirectMessagePublisher.publish("/server/directRoom/" + room.getId(), payload);
                });
    }
}
