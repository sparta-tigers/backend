package com.sparta.spartatigers.domain.support.chat.service;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.foundation.user.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardConnection;
import com.sparta.spartatigers.domain.foundation.baseball.match.repository.LiveBoardConnectionRepository;
import com.sparta.spartatigers.domain.support.chat.interceptor.StompPrincipal;
import com.sparta.spartatigers.domain.support.chat.model.ChatMessage;
import com.sparta.spartatigers.domain.support.chat.pubsub.RedisChatPublisher;
import com.sparta.spartatigers.domain.support.chat.pubsub.RedisChatSubscriber;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final RedisChatPublisher redisChatPublisher;
	private final RedisChatSubscriber redisChatSubscriber;
	private final RedisMessageListenerContainer redisMessageListener;
	private final Map<String, ChannelTopic> topics =
		new ConcurrentHashMap<>(); // 채팅방별 topic
	private final UserRepository userRepository;
	private final LiveBoardConnectionRepository liveBoardConnectionRepository;
	private final FavTeamRepository favTeamRepository;
	private final Clock clock;

	public void sendGroupMessage(ChatMessage message, Principal principal) {
		if (!(principal instanceof StompPrincipal stompPrincipal)) {
			throw new InvalidRequestException(ExceptionCode.WEBSOCKET_UNAUTHORIZED);
		}

		Long senderId = Long.parseLong(stompPrincipal.getName());
		String nickname = userRepository.findNicknameById(senderId).orElse("회원");

		String symbolUrl = favTeamRepository.findByUserId(senderId)
			.map(favoriteTeam -> favoriteTeam.getTeam().getSymbolUrl())
			.orElse(null);

		ChatMessage sendMessage = ChatMessage.ofLiveBoardRoom(
			message.getRoomId(),
			senderId,
			nickname,
			message.getContent(),
			symbolUrl,
			LocalDateTime.now(clock),
			message.getTempId()
		);

		ChannelTopic topic = getOrInitTopic(message.getRoomId());
		redisChatPublisher.publish(topic, sendMessage);
	}

	public void enterRoom(Message<ChatMessage> message, Principal principal) {
		// 기본값..
		Long senderId = null;
		String nickname = "비회원";
		if (principal instanceof StompPrincipal) {
			senderId = getSenderId(principal);
			nickname = userRepository.findNicknameById(senderId).orElse("비회원");
		}

		String globalSessionId = getGlobalSessionId(message);
		String roomId = message.getPayload().getRoomId();

		LiveBoardConnection connection = LiveBoardConnection.of(
			globalSessionId, senderId, nickname, roomId, LocalDateTime.now(clock)
		);
		liveBoardConnectionRepository.saveConnection(roomId,globalSessionId, connection);
	}

	public void exitRoom(Message<ChatMessage> message) {
		String roomId = message.getPayload().getRoomId();
		String globalSessionId = getGlobalSessionId(message);

		liveBoardConnectionRepository.deleteConnection(roomId,globalSessionId);
	}

	private ChannelTopic getOrInitTopic(String roomId) {
		return topics.computeIfAbsent(
			roomId,
			key -> {
				ChannelTopic topic = new ChannelTopic(key);
				redisMessageListener.addMessageListener(redisChatSubscriber, topic);
				return topic;
			});
	}

	private Long getSenderId(Principal principal){
		if(principal instanceof StompPrincipal stompPrincipal) {
			return Long.parseLong(stompPrincipal.getName());
		}
		// TODO : 나중에 소셜 로그인 추가되면 추가하기
		return null;
	}

	private String getGlobalSessionId(Message<ChatMessage> message) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
		return accessor.getSessionId();
	}

	public void handleDisconnect(String globalSessionId) {
		List<String> roomIds = liveBoardConnectionRepository.findAllRoomIds();

		for (String roomId : roomIds) {
			Map<Object, Object> connections = liveBoardConnectionRepository.findAllConnections(roomId);
			if (connections.containsKey(globalSessionId)) {
				liveBoardConnectionRepository.deleteConnection(roomId, globalSessionId);
			}
		}
	}


}
