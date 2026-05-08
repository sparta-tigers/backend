package com.sparta.spartatigers.domain.dashboard.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.dashboard.dto.HomeDashboardResponseDto;
import com.sparta.spartatigers.domain.favoriteteam.model.entity.FavoriteTeam;
import com.sparta.spartatigers.domain.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.domain.match.model.LeagueType;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final FavTeamRepository favTeamRepository;

    public HomeDashboardResponseDto getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));

        // 🚨 앙드레 카파시: 결정론적 시간 처리를 위해 메서드 진입점에서 시각 캡처
        LocalDateTime now = LocalDateTime.now();

        // 1. 입학 일수 계산 (createdAt 기준)
        long enrollmentDays = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), now.toLocalDate()) + 1;

        // 2. 응원 팀 및 남은 경기 수 조회
        FavoriteTeam favTeam = favTeamRepository.findByUserId(userId).orElse(null);
        
        Long remainingMatches = 0L;
        String favoriteTeamCode = null;

        if (favTeam != null) {
            // 🚨 앙드레 카파시: NPE 방어 - Team 및 Code 존재 여부 확인
            com.sparta.spartatigers.domain.team.model.TeamCode teamCode = 
                (favTeam.getTeam() != null && favTeam.getTeam().getCode() != null) 
                ? favTeam.getTeam().getCode() 
                : null;
            
            favoriteTeamCode = (teamCode != null) ? teamCode.name() : null;

            remainingMatches = matchRepository.countRemainingMatches(
                    favTeam.getTeam().getId(),
                    LeagueType.REGULAR,
                    now
            );
        }

        return HomeDashboardResponseDto.builder()
                .nickname(user.getNickname())
                .enrollmentDays(enrollmentDays)
                .remainingMatches(remainingMatches)
                .favoriteTeamCode(favoriteTeamCode)
                .build();
    }
}
