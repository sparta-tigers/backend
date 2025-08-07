package com.sparta.spartatigers.domain.stompchat.Controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.sparta.spartatigers.domain.stompchat.Service.LiveboardChatService;
import com.sparta.spartatigers.domain.stompchat.model.ChatMessage;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StompChatController {

	private LiveboardChatService liveboardChatService;

	@MessageMapping("/liveboard/send")
	public void sendMessage(ChatMessage message, Principal principal) {
		liveboardChatService.handleMessage(message, principal);
	}
}
