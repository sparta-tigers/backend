package com.sparta.spartatigers.domain.stompchat.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.sparta.spartatigers.domain.directRoom.registry.RedisUserSessionRegistry;
import com.sparta.spartatigers.domain.stompchat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

	private final ChatService liveBoardService;
	private final RedisUserSessionRegistry sessionRegistry;

	// 다이렉트룸 구독 감지
	@EventListener
	public void handleSessionSubscriveEvent (SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = accessor.getDestination();
		Long userId = getUserIdFromAccessor(accessor);

		if(destination != null && destination.startsWith("/server/directRoom/") && userId != null) {
			Long roomId = Long.parseLong(destination.substring("/server/directRoom/".length()));
			sessionRegistry.registerUserInRoom(roomId, userId);
			log.info("[1:1 채팅] 입장 이벤트 감지 - user {} entered room {}", userId, roomId);
		}
	}

	// 다이렉트룸 구독 종료 감지
	@EventListener
	public void handleUnsubsribe (SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = accessor.getDestination();
		Long userId = getUserIdFromAccessor(accessor);

		if(destination != null && destination.startsWith("/server/directRoom/") && userId != null) {
			Long roomId = Long.parseLong(destination.substring("/server/directRoom/".length()));
			sessionRegistry.unregisterUserInRoom(roomId, userId);
			log.info("[1:1 채팅] 퇴장 이벤트 감지 - user {} left room {}", userId, roomId);
		}
	}

	// 웹소켓 연결 종료 감지
	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		Long userId = sessionRegistry.getUserIdBySessionId(sessionId);
		if(userId != null) {
			liveBoardService.handleDisconnect(sessionId);
			sessionRegistry.unregisterSession(userId, sessionId);
		}
	}

	private Long getUserIdFromAccessor(StompHeaderAccessor accessor) {
		Object userIdAttributes = accessor.getSessionAttributes().get("userId");
		if(userIdAttributes != null) {
			return Long.parseLong(userIdAttributes.toString());
		}
		String sessionId = accessor.getSessionId();
		Long userId = sessionRegistry.getUserIdBySessionId(sessionId);

		return userId;
	}
}
