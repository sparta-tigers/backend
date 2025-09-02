package com.sparta.spartatigers.domain.stompchat.interceptor;


import com.sparta.spartatigers.domain.directRoom.registry.RedisUserSessionRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.auth.service.JwtTokenService;
import com.sparta.spartatigers.domain.stompchat.model.ChatDomainType;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

	// 1:1채팅만 인터셉터에서 진행하는 추가적인 로직이 있으므로 그거만 처리하면 될듯
	private static final String CHAT_DOMAIN_TYPE = "ChatDomain";
	private final JwtTokenService jwtTokenService;
	private final UserRepository userRepository;
	private final RedisUserSessionRegistry userSessionRegistry;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
			MessageHeaderAccessor.getAccessor(
				message, StompHeaderAccessor.class); // stomp 메세지의 헤더를 분석 ( 커멘드, 세션아이디 등등..)
		StompCommand command = accessor.getCommand();

		String domainRaw = accessor.getFirstNativeHeader(CHAT_DOMAIN_TYPE);
		ChatDomainType domain = resolveDomain(domainRaw);

		if (StompCommand.CONNECT.equals(command)) {
			String token = accessor.getFirstNativeHeader("Authorization");

			// 토큰 있음
			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);
				TokenClaim claims = jwtTokenService.parseAccessToken(token);
				if (claims != null) {
					String email = claims.getSubject();
					User user = userRepository.findByEmail(email).orElseThrow();
					String userId = String.valueOf(user.getId());
					String nickname = user.getNickname();

					// 웹소켓에 사용자 등록
					StompPrincipal principal = new StompPrincipal(userId, nickname);
					accessor.setUser(principal);

					if(domain.equals(ChatDomainType.EXCHANGE)){
						userSessionRegistry.registerSession(user.getId(), accessor.getSessionId());
					}
				}
			}
		}
		return message;
	}

	private ChatDomainType resolveDomain(String domainRaw) {
		if ("liveboard".equalsIgnoreCase(domainRaw)) return ChatDomainType.LIVEBOARD;
		if ("directroom".equalsIgnoreCase(domainRaw)) return ChatDomainType.EXCHANGE;
		if ("location".equalsIgnoreCase(domainRaw)) return ChatDomainType.LOCATION;
		throw new RuntimeException("ChatDomain 헤더가 올바르지 않습니다.");
	}

}
