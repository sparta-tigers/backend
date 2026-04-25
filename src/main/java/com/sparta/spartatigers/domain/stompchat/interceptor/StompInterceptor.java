package com.sparta.spartatigers.domain.stompchat.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.auth.service.JwtTokenService;
import com.sparta.spartatigers.domain.directRoom.registry.RedisUserSessionRegistry;
import com.sparta.spartatigers.domain.stompchat.model.ChatDomainType;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		if (StompCommand.CONNECT.equals(command)) {
			// [DEBUG] CONNECT 시도 감지 로그
			log.info("[StompInterceptor] CONNECT 시도 감지 - sessionId: {}", accessor.getSessionId());

			// [FIX] 인증(Authentication)을 도메인 검증(Authorization)보다 먼저 수행
			// 순서: 1. 토큰 추출 및 검증 → 2. 도메인 해석
			String rawToken = accessor.getFirstNativeHeader("Authorization");

			// [FIX] 토큰 없음 또는 형식 오류 → 명시적 예외로 CONNECT 거절 (인증 우회 방지)
			if (rawToken == null || !rawToken.startsWith("Bearer ")) {
				log.warn("[StompInterceptor] 토큰 없음 또는 형식 오류 - sessionId: {}, Authorization 헤더: {}", accessor.getSessionId(), rawToken);
				throw new IllegalArgumentException("WebSocket CONNECT 시 Authorization Bearer 토큰이 필요합니다.");
			}

			String token = rawToken.substring(7);

			// [FIX] parseAccessToken은 실패 시 항상 InvalidRequestException을 throw하므로
			// try-catch로 감싸 일관된 예외 처리를 보장한다 (dead code였던 null 체크 제거)
			TokenClaim claims;
			try {
				claims = jwtTokenService.parseAccessToken(token);
			} catch (Exception e) {
				log.warn("[StompInterceptor] JWT 검증 실패 - sessionId: {}, 원인: {}", accessor.getSessionId(), e.getMessage());
				throw new IllegalArgumentException("WebSocket CONNECT 토큰 검증에 실패했습니다.");
			}

			String email = claims.getSubject();
			User user = userRepository.findByEmail(email).orElseThrow();
			String userId = String.valueOf(user.getId());
			String nickname = user.getNickname();

			// 웹소켓에 사용자 등록
			StompPrincipal principal = new StompPrincipal(userId, nickname);
			accessor.setUser(principal);

			// [FIX] 인증 완료 후 도메인 해석 (인증 → 인가 순서 준수)
			String domainRaw = accessor.getFirstNativeHeader(CHAT_DOMAIN_TYPE);
			ChatDomainType domain = resolveDomain(domainRaw);

			if (domain.equals(ChatDomainType.EXCHANGE)) {
				userSessionRegistry.registerSession(user.getId(), accessor.getSessionId());
			}
		}
		return message;
	}

	private ChatDomainType resolveDomain(String domainRaw) {
		if (domainRaw == null || domainRaw.isBlank()) {
			throw new IllegalArgumentException("ChatDomain 헤더가 필수입니다. 클라이언트 STOMP connectHeaders에 ChatDomain을 명시하세요.");
		}
		if ("liveboard".equalsIgnoreCase(domainRaw)) return ChatDomainType.LIVEBOARD;
		if ("directroom".equalsIgnoreCase(domainRaw)) return ChatDomainType.EXCHANGE;
		if ("location".equalsIgnoreCase(domainRaw)) return ChatDomainType.LOCATION;
		throw new IllegalArgumentException("지원하지 않는 ChatDomain: '" + domainRaw + "'. 허용값: liveboard, directroom, location");
	}

}
