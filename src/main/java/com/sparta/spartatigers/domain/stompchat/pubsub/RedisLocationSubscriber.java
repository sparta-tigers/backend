package com.sparta.spartatigers.domain.stompchat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.stompchat.dto.response.LocationUpdateDto;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLocationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            LocationUpdateDto location = objectMapper.readValue(json, LocationUpdateDto.class);

            String redisChannel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String stadiumId = extractStadiumId(redisChannel);

            if (stadiumId != null) {
                String destination = "/server/location/stadium/" + stadiumId;
                messagingTemplate.convertAndSend(destination, location);
            } else {
                throw new IllegalArgumentException("유효하지 않은 채널");
            }
        } catch (Exception e) {
            throw new RuntimeException("RedisLocationSubscriber 오류 발생", e);
        }
    }

    private String extractStadiumId(String redisChannel) {
        if (redisChannel.startsWith("location:stadium:")) {
            return redisChannel.substring("location:stadium:".length());
        }
        return null;
    }
}
