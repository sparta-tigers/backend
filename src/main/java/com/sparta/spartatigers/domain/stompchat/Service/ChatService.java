package com.sparta.spartatigers.domain.stompchat.Service;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.stompchat.model.ChatMessage;
import com.sparta.spartatigers.domain.stompchat.pubsub.RedisChatPublisher;
import com.sparta.spartatigers.domain.stompchat.pubsub.RedisChatSubscriber;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;

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


	public void sendGroupMessage(ChatMessage message, Principal principal) {
		User sender = userRepository.findById(message.getSenderId()).orElseThrow();

		ChatMessage sendMessage = ChatMessage.ofLiveBoardRoom(message.getRoomId(),sender.getId(), sender.getNickname(),
			message.getContent());

		ChannelTopic topic = getOrInitTopic(message.getRoomId());
		redisChatPublisher.publish(topic,sendMessage);
	}

	public void sendDirectMessage(ChatMessage message, Principal principal) {

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
		return Long.valueOf(principal.getName());
	}
}
