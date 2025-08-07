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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final RedisChatPublisher redisChatPublisher;
	private final RedisChatSubscriber redisChatSubscriber;
	private final RedisMessageListenerContainer redisMessageListener;
	private final Map<String, ChannelTopic> topics =
		new ConcurrentHashMap<>(); // 채팅방별 topic


	public void sendGroupMessage(ChatMessage message, Principal principal) {
		ChannelTopic topic = getOrInitTopic(message.getRoomId());
		redisChatPublisher.publish(topic,message);
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
