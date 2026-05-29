package com.sparta.spartatigers.domain.support.chat.pubsub;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisChatPublisher {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public void publish(ChannelTopic topic, Object message) {
		try {
			String json = objectMapper.writeValueAsString(message);
			redisTemplate.convertAndSend(topic.getTopic(), json);
		} catch (Exception e) {
			throw new RuntimeException("redis 직렬화 실패",e);
		}

	}
}
