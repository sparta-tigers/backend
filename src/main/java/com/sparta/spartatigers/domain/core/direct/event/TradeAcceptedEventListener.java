package com.sparta.spartatigers.domain.core.direct.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sparta.spartatigers.domain.core.direct.service.DirectRoomService;
import com.sparta.spartatigers.domain.foundation.common.event.TradeAcceptedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeAcceptedEventListener {

    private final DirectRoomService directRoomService;

    @EventListener
    public void handleTradeAcceptedEvent(TradeAcceptedEvent event) {
        log.info("[TradeAcceptedEventListener] 채팅방 생성 이벤트 수신 - exchangeRequestId: {}", event.exchangeRequestId());
        directRoomService.createRoom(event.exchangeRequestId(), event.responderId());
    }
}
