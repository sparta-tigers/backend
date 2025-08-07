package com.sparta.spartatigers.domain.stompchat.Controller;

import org.springframework.stereotype.Controller;

import com.sparta.spartatigers.domain.stompchat.Service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExchangeChatController {

	private final ChatService chatService;


}
