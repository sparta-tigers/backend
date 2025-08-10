package com.sparta.spartatigers.domain.stompchat.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

	//private static final String CHAT_DOMAIN_TYPE = "ChatDomain";
	// 채팅 기능별로 헤더에 도메인 넣으려고 했는데 컨트롤러에서 권한 처리가 나을듯,,,


	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		return message;
	}
}
