package com.sparta.spartatigers.domain.support.chat.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.support.chat.service.LocationService;
import com.sparta.spartatigers.domain.support.chat.dto.response.RedisUpdateDto;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLocationSubscriber implements MessageListener {

    private static final double NEARBY_RADIUS_KM = 0.05;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationService locationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            RedisUpdateDto location = objectMapper.readValue(json, RedisUpdateDto.class);
            Long userId = location.getUserId();

            List<Long> nearByUserIds = locationService.findUsersNearBy(userId, NEARBY_RADIUS_KM);
            nearByUserIds.forEach(
                targetUserId -> {
                    String destination = "/server/items/user/" + targetUserId;
                    messagingTemplate.convertAndSend(destination,
                        Map.of("type", "USER_LOCATION_UPDATE", "data", location));
                }
            );
            String myDestination = "/server/items/user/" + userId;
            messagingTemplate.convertAndSend(myDestination, Map.of("type", "REFRESH_ITEMS"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 메시지 역직렬화 실패", e);
        }

    }
}
