package com.sparta.spartatigers.domain.stompchat.Controller;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.sparta.spartatigers.domain.stompchat.Service.ChatService;
import com.sparta.spartatigers.domain.stompchat.model.ChatMessage;
import com.sparta.spartatigers.global.exception.WebSocketException;
import com.sparta.spartatigers.global.response.WebSocketErrorResponse;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LiveBoardChatController {

	private final ChatService chatService;

	// 채팅
	@MessageMapping("/liveboard/send")
	public void sendMessage(ChatMessage message, Principal principal) {
		chatService.sendGroupMessage(message, principal);
	}

	// 입장
	@MessageMapping("/liveboard/enter")
	public void enterLiveBoard(Message<ChatMessage> message, Principal principal) {
		chatService.enterRoom(message, principal);
	}

	// 퇴장
	@MessageMapping("/liveboard/exit")
	public void exitLiveBoard(Message<ChatMessage> message) {
		chatService.exitRoom(message);
	}

	// 예외 처리
	@MessageExceptionHandler(WebSocketException.class)
	@SendToUser("/liveboard/errors")
	public WebSocketErrorResponse handleWebSocketError(WebSocketException e) {
		return WebSocketErrorResponse.from(e.getCode());
	}

}
