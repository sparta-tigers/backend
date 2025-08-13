package com.sparta.spartatigers.domain.stompchat.Controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.sparta.spartatigers.domain.stompchat.Service.ChatService;
import com.sparta.spartatigers.domain.stompchat.model.ChatMessage;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExchangeChatController {

	private final ChatService chatService;

	@MessageMapping("/directRoom/send")
	public void sendMessage(ChatMessage message, Principal principal) {
		chatService.sendDirectMessage(message, principal);
	}


}
