package com.sparta.spartatigers.domain.stompchat.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.sparta.spartatigers.domain.stompchat.Service.ChatService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final ChatService liveBoardService;

	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		liveBoardService.handleDisconnect(sessionId);
	}
}
