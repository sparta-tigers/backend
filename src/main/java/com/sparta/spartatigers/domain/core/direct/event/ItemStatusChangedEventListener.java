package com.sparta.spartatigers.domain.core.direct.event;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    /**
     * 거래 완료/취소 시 채팅방 DB 상태를 갱신한다.
     * - 동일 트랜잭션 내에서 동기적으로 실행되어, 거래 상태 변경과 채팅방 종료가 하나의 커밋으로 보장된다.
     * - 실패 시 메인 트랜잭션까지 롤백되어 데이터 불일치를 방지한다.
     */
    @EventListener
    @Transactional
    public void handleRoomStatusUpdate(ItemStatusChangedEvent event) {
        if (!"거래가 완료되었습니다.".equals(event.getMessage())) {
            return;
        }
        directRoomRepository.findByExchangeRequestId(event.getExchangeRequestId())
                .ifPresent(room -> {
                    log.info("[ItemStatusChangedEventListener] 채팅방 종료 처리 - exchangeRequestId: {}",
                            event.getExchangeRequestId());
                    room.complete();
                });
    }

    /**
     * 거래 상태 변경 후 Redis 를 통해 클라이언트에 실시간 알림을 발송한다.
     * - AFTER_COMMIT: DB 커밋이 완료된 이후에만 실행되어 클라이언트가 최신 상태를 읽을 수 있다.
     * - @Async: 외부 I/O(Redis)를 별도 스레드로 분리하여 메인 응답 지연을 방지한다.
     * - 이 메서드의 실패는 DB 정합성에 영향을 주지 않는다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomNotification(ItemStatusChangedEvent event) {
        directRoomRepository.findByExchangeRequestId(event.getExchangeRequestId())
                .ifPresent(room -> {
                    log.info("[ItemStatusChangedEventListener] Redis 알림 발송 - exchangeRequestId: {}",
                            event.getExchangeRequestId());
                    Map<String, Object> payload = Map.of(
                            "type", "SYSTEM",
                            "action", "STATUS_UPDATED",
                            "roomId", room.getId());
                    redisDirectMessagePublisher.publish("/server/directRoom/" + room.getId(), payload);
                });
    }
}
