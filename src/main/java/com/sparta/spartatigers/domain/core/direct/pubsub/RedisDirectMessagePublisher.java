package com.sparta.spartatigers.domain.core.direct.pubsub;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDirectMessagePublisher {

    // TODO: pub/sub합체오줌
    private final StringRedisTemplate redisStringTemplate;
    private final ObjectMapper objectMapper;

    /** 주어진 senderId와 ChatMessageRequest의 내용으로 JSON 생성하고, 지정된 Redis 채널로 메시지를 발행합니다. */
    public void publish(String channel, Object messagePayload) {
        try {
            String json = objectMapper.writeValueAsString(messagePayload);
            redisStringTemplate.convertAndSend(channel, json);
        } catch (Exception e) {
            log.error("Redis 발행 실패", e);
        }
    }
}
