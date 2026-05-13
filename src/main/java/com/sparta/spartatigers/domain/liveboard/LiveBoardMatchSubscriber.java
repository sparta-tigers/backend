package com.sparta.spartatigers.domain.liveboard;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.liveboard.dto.LineupCacheDto;
import com.sparta.spartatigers.domain.liveboard.model.LiveBoardData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveBoardMatchSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.sparta.spartatigers.domain.startinglineup.service.StartingLineupService startingLineupService;
    private final LiveBoardMatchService liveBoardMatchService;

    private static final String LINEUP_CACHE_PREFIX = "lineup:match:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            LiveBoardData liveBoardData = objectMapper.readValue(body, LiveBoardData.class);

            // 1. 실시간 매치 정보 STOMP 발행
            messagingTemplate.convertAndSend(
                    "/server/liveboard/room/" + liveBoardData.getMatchId() + "/match", liveBoardData);

            // 2. 라인업 데이터 경량화 및 캐싱 (Phase 13)
            cacheLineupData(liveBoardData);

            // 3. 경기 점수 DB 동기화 (Phase 24) - 별도 서비스 호출로 트랜잭션 보장
            liveBoardMatchService.updateMatchScore(liveBoardData);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse LiveBoardData from Redis message: {}", e.getMessage(), e);
        }
    }

    /**
     * 라인업 데이터를 추출하여 Redis에 경량화 캐싱
     * 🚨 앙드레 카파시: 무거운 전체 데이터를 피하고 필요한 정보만 선별적으로 캐싱하여 메모리 절약.
     */
    private void cacheLineupData(LiveBoardData data) {
        if (data.getMatchId() == null) return;

        LineupCacheDto newCache = LineupCacheDto.builder()
                .matchId(data.getMatchId())
                .awayBatters(data.getAwayBatters())
                .homeBatters(data.getHomeBatters())
                .build();

        // 빈 배열이거나 데이터가 아예 없는 경우 업데이트 건너뜀
        if (!newCache.isNotEmpty()) {
            return;
        }

        String cacheKey = LINEUP_CACHE_PREFIX + data.getMatchId();
        
        // 중복 쓰기 방지: 기존 데이터와 비교
        Object existingData = redisTemplate.opsForValue().get(cacheKey);
        if (existingData != null) {
            try {
                // 🚨 앙드레 카파시: 안전한 역직렬화 (단순 캐스팅 금지)
                LineupCacheDto cached = objectMapper.convertValue(existingData, LineupCacheDto.class);
                if (newCache.equals(cached)) {
                    log.debug("Lineup data for matchId {} is identical to existing cache, skipping update", data.getMatchId());
                    return; // 변경사항 없음
                }
            } catch (IllegalArgumentException e) {
                log.warn("Failed to compare existing lineup cache, proceeding with update", e);
            }
        }

        redisTemplate.opsForValue().set(cacheKey, newCache, CACHE_TTL);
        log.info("Successfully updated lineup cache for matchId: {}", data.getMatchId());

        // 3. DB 영속화 (Fallback 대응)
        try {
            startingLineupService.saveLineupFromCache(newCache);
        } catch (Exception e) {
            log.error("Failed to save lineup to DB for matchId: {}", data.getMatchId(), e);
        }
    }

}