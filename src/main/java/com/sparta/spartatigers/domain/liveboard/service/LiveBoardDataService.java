package com.sparta.spartatigers.domain.liveboard.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.liveboard.model.LiveBoardData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * 실시간 중계 데이터(문자중계 등)를 Redis 캐시에 저장하고 조회하는 서비스
 * 
 * Why: WebSocket으로 발행되는 데이터를 REST API에서도 초기 로드용으로 제공하기 위함.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveBoardDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DATA_CACHE_PREFIX = "liveboard:data:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    /**
     * 실시간 중계 데이터 캐싱
     */
    public void cacheLiveBoardData(LiveBoardData data) {
        if (data == null || data.getMatchId() == null) return;

        String key = DATA_CACHE_PREFIX + data.getMatchId();
        redisTemplate.opsForValue().set(key, data, CACHE_TTL);
    }

    /**
     * 캐싱된 실시간 중계 데이터 조회
     */
    public LiveBoardData getLiveBoardData(Long matchId) {
        if (matchId == null) {
            return null;
        }
        String key = DATA_CACHE_PREFIX + matchId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, LiveBoardData.class);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve liveboard data cache for matchId: {}", matchId, e);
        }
        return null;
    }
}
