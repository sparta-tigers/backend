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

        // 1. 입학 일수 계산 (createdAt 기준)
        long enrollmentDays = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDateTime.now().toLocalDate()) + 1;

        // 2. 응원 팀 및 남은 경기 수 조회
        FavoriteTeam favTeam = favTeamRepository.findByUserId(userId).orElse(null);
        
        Long remainingMatches = 0L;
        String favoriteTeamCode = null;

        if (favTeam != null) {
            favoriteTeamCode = favTeam.getTeam().getCode().name();
            remainingMatches = matchRepository.countRemainingMatches(
                    favTeam.getTeam().getId(),
                    LeagueType.REGULAR,
                    LocalDateTime.now()
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
