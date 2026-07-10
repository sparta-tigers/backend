package com.sparta.spartatigers.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.sparta.spartatigers.domain.support.chat.interceptor.StompInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompInterceptor stompInterceptor;

	// stomp 연결을 위한 앤드포인트 등록
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
				.setAllowedOriginPatterns("*")
				.withSockJS();
	}

	// 메세지 브로커 설정
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/server");
		registry.setApplicationDestinationPrefixes("/client");
	}

	// 클라이언트 -> 서버로 들어오는 메세지 처리할 채널 설정 (인터셉터 등록 / 인증인가 관련)
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(
				stompInterceptor);
	}
}
