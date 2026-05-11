package com.sparta.spartatigers.domain.dashboard.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.dashboard.dto.HomeDashboardResponseDto;
import com.sparta.spartatigers.domain.favoriteteam.model.entity.FavoriteTeam;
import com.sparta.spartatigers.domain.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.domain.liveboard.dto.LineupCacheDto;
import com.sparta.spartatigers.domain.liveboard.model.LineupBatter;
import com.sparta.spartatigers.domain.liveboard.match.model.LeagueType;
import com.sparta.spartatigers.domain.liveboard.match.model.MatchResult;
import com.sparta.spartatigers.domain.liveboard.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final FavTeamRepository favTeamRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private static final String LINEUP_CACHE_PREFIX = "lineup:match:";

    public HomeDashboardResponseDto getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);

        // 1. 입학 일수 계산
        long enrollmentDays = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), now.toLocalDate()) + 1;

        // 2. 응원 팀 및 관련 정보 조회
        FavoriteTeam favTeam = favTeamRepository.findByUserId(userId).orElse(null);
        
        Long remainingMatches = 0L;
        String favoriteTeamCode = null;
        List<LineupBatter> todayLineup = Collections.emptyList();

        if (favTeam != null && favTeam.getTeam() != null) {
            favoriteTeamCode = (favTeam.getTeam().getCode() != null) ? favTeam.getTeam().getCode().name() : null;

            // 남은 경기 수 조회
            remainingMatches = matchRepository.countRemainingMatches(
                    favTeam.getTeam().getId(),
                    LeagueType.REGULAR,
                    now,
                    MatchResult.NOT_PLAYED
            );

            // 3. 오늘의 라인업 조회 (Phase 13)
            todayLineup = fetchTodayLineup(favTeam.getTeam().getId(), now);
        }

        return HomeDashboardResponseDto.builder()
                .nickname(user.getNickname())
                .enrollmentDays(enrollmentDays)
                .remainingMatches(remainingMatches)
                .favoriteTeamCode(favoriteTeamCode)
                .todayLineup(todayLineup)
                .build();
    }

    /**
     * 오늘 경기 중 해당 팀의 라인업만 추출하여 반환
     * 🚨 앙드레 카파시: DB 부하 없이 Redis 캐시만 활용하며, 유저 응원 팀 위치(Home/Away)에 따라 필터링된 결과만 제공.
     */
    private List<LineupBatter> fetchTodayLineup(Long teamId, LocalDateTime now) {
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 오늘 진행되는 해당 팀의 정규 리그 경기 조회 (최적화: LIMIT 1)
        return matchRepository.findFirstByMatchTimeBetweenAndTeam(startOfDay, endOfDay, teamId, LeagueType.REGULAR)
                .map(match -> {
                    String cacheKey = LINEUP_CACHE_PREFIX + match.getId();
                    Object cachedData = redisTemplate.opsForValue().get(cacheKey);
                    
                    if (cachedData == null) {
                        return Collections.<LineupBatter>emptyList();
                    }

                    try {
                        // 🚨 앙드레 카파시: ObjectMapper를 이용한 명시적 타입 변환 (안전한 역직렬화)
                        LineupCacheDto lineupCache = objectMapper.convertValue(cachedData, LineupCacheDto.class);
                        
                        // 응원 팀이 홈인지 어웨이인지에 따라 필터링
                        List<LineupBatter> lineup = match.getHomeTeam().getId().equals(teamId) 
                                ? lineupCache.getHomeBatters() 
                                : lineupCache.getAwayBatters();
                                
                        return (lineup != null) ? lineup : Collections.<LineupBatter>emptyList();
                    } catch (Exception e) {
                        log.error("Failed to parse lineup cache for matchId: {}", match.getId(), e);
                        return Collections.<LineupBatter>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }
}
