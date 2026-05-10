package com.sparta.spartatigers.domain.liveboard;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.liveboard.dto.LineupCacheDto;
import com.sparta.spartatigers.domain.liveboard.dto.LineupResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 라인업 조회 전용 서비스
 *
 * Why: Redis 캐시(lineup:match:{matchId})를 읽어 프론트엔드에 홈/어웨이 라인업을 제공.
 * 트랜잭션 없이 동작하며, Redis opsForValue().get 1회만 수행.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineupQueryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String LINEUP_CACHE_PREFIX = "lineup:match:";

    /**
     * matchId 기반 라인업 조회
     *
     * @param matchId 경기 ID
     * @return 홈/어웨이 라인업 응답 (캐시 없으면 빈 배열)
     */
    public LineupResponseDto getMatchLineup(Long matchId) {
        String cacheKey = LINEUP_CACHE_PREFIX + matchId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached == null) {
            return LineupResponseDto.empty(matchId);
        }

        try {
            LineupCacheDto cacheDto = objectMapper.convertValue(cached, LineupCacheDto.class);
            return LineupResponseDto.from(cacheDto);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse lineup cache for matchId: {}", matchId, e);
            return LineupResponseDto.empty(matchId);
        }
    }
}
