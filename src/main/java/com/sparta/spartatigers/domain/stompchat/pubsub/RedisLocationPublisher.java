package com.sparta.spartatigers.domain.stompchat.pubsub;

import com.sparta.spartatigers.domain.stompchat.dto.response.RedisUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLocationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publishLocation(RedisUpdateDto dto) {
        redisTemplate.convertAndSend("location-channel", dto);
    }
}
