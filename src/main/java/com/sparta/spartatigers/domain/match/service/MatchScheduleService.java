package com.sparta.spartatigers.domain.match.service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.favoriteteam.model.entity.FavoriteTeam;
import com.sparta.spartatigers.domain.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.domain.match.dto.MatchScheduleResponseDto;
import com.sparta.spartatigers.domain.match.model.HomeAway;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScheduleService {

    private final MatchRepository matchRepository;
    private final FavTeamRepository favTeamRepository;

    /**
     * 특정 사용자의 응원 팀 기준 월별 경기 일정을 조회함.
     * 
     * @param userId 사용자 ID
     * @param year   조회 연도
     * @param month  조회 월
     * @return 가공된 경기 일정 리스트
     */
    @Transactional(readOnly = true)
    public List<MatchScheduleResponseDto> getMonthlySchedule(Long userId, int year, int month) {
        // 1. 사용자의 응원 팀 조회 (응원 팀이 없으면 빈 일정 반환)
        Optional<FavoriteTeam> favoriteTeamOpt = favTeamRepository.findByUserId(userId);
        if (favoriteTeamOpt.isEmpty()) {
            log.info("User {} has no favorite team. Returning empty match schedule.", userId);
            return Collections.emptyList();
        }
        FavoriteTeam favoriteTeam = favoriteTeamOpt.get();
        Team myTeam = favoriteTeam.getTeam();

        // 2. 해당 월의 시작과 끝 시간 계산
        LocalDateTime startOfMonth;
        try {
            startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        } catch (DateTimeException e) {
            throw new InvalidRequestException(ExceptionCode.INVALID_TYPE_EXCEPTION);
        }
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        // 3. 경기 데이터 조회 (팀 조건 포함 및 JOIN FETCH를 통한 최적화된 쿼리 사용)
        List<Match> matches = matchRepository.findAllByMatchTimeBetweenAndTeam(startOfMonth, endOfMonth, myTeam.getId());

        // 4. DTO 변환
        return matches.stream()
                .map(m -> convertToDto(m, myTeam))
                .collect(Collectors.toList());
    }

    private MatchScheduleResponseDto convertToDto(Match match, Team myTeam) {
        boolean isHome = match.getHomeTeam().getId().equals(myTeam.getId());
        Team opponent = isHome ? match.getAwayTeam() : match.getHomeTeam();

        return MatchScheduleResponseDto.builder()
                .day(match.getMatchTime().getDayOfMonth())
                .opponentCode(opponent.getCode().getDescriptiveCode())
                .location(isHome ? HomeAway.HOME : HomeAway.AWAY)
                .timeText(match.getMatchTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build();
    }
}
