package com.sparta.spartatigers.domain.liveboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.liveboard.model.LiveBoardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveBoardMatchSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            LiveBoardData liveBoardData =
                    objectMapper.readValue(
                            new String(message.getBody(), StandardCharsets.UTF_8),
                            LiveBoardData.class);

            messagingTemplate.convertAndSend(
                    "/server/liveboard/room/" + liveBoardData.getMatchId() + "/match", liveBoardData);
            log.info("/server/liveboard/room/{}/match pusehd", liveBoardData.getMatchId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}