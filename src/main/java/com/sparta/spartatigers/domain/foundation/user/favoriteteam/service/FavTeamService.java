package com.sparta.spartatigers.domain.foundation.user.favoriteteam.service;

import com.sparta.spartatigers.domain.foundation.baseball.team.model.Team;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.TeamCode;
import com.sparta.spartatigers.domain.foundation.baseball.team.repository.TeamRepository;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.dto.FavTeamRequestDto;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.dto.FavTeamResponseDto;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.model.entity.FavoriteTeam;
import com.sparta.spartatigers.domain.foundation.user.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavTeamService {

    private final FavTeamRepository favTeamRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public FavTeamResponseDto add(FavTeamRequestDto request, Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        Team team;
        if (request.getTeamId() != null) {
            team = teamRepository.findByIdOrElseThrow(request.getTeamId());
        } else if (request.getTeamCode() != null) {
            try {
                team = teamRepository.findByCodeOrElseThrow(
                    TeamCode.valueOf(request.getTeamCode().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException(
                    ExceptionCode.INVALID_TYPE_EXCEPTION
                );
            }
        } else {
            throw new InvalidRequestException(
                ExceptionCode.INVALID_TYPE_EXCEPTION
            );
        }

        FavoriteTeam favoriteTeam = FavoriteTeam.from(user, team);

        try {
            favTeamRepository.saveAndFlush(favoriteTeam);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidRequestException(
                ExceptionCode.ALREADY_EXISTS_FAVORITE_TEAM
            );
        }

        return FavTeamResponseDto.of(favoriteTeam);
    }

    @Transactional(readOnly = true)
    public FavTeamResponseDto get(Long userId) {
        return favTeamRepository
            .findByUserId(userId)
            .map(FavTeamResponseDto::of)
            .orElse(null);
    }

    @Transactional
    public FavTeamResponseDto update(FavTeamRequestDto request, Long userId) {
        FavoriteTeam favoriteTeam = favTeamRepository.findByUserIdOrElseThrow(
            userId
        );

        Team newTeam;
        if (request.getTeamId() != null) {
            newTeam = teamRepository.findByIdOrElseThrow(request.getTeamId());
        } else if (request.getTeamCode() != null) {
            try {
                newTeam = teamRepository.findByCodeOrElseThrow(
                    TeamCode.valueOf(request.getTeamCode().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException(
                    ExceptionCode.INVALID_TYPE_EXCEPTION
                );
            }
        } else {
            throw new InvalidRequestException(
                ExceptionCode.INVALID_TYPE_EXCEPTION
            );
        }

        favoriteTeam.update(newTeam);
        return FavTeamResponseDto.of(favoriteTeam);
    }

    @Transactional
    public Void delete(Long userId) {
        userRepository.findByIdOrElseThrow(userId);
        FavoriteTeam favoriteTeam = favTeamRepository.findByUserIdOrElseThrow(
            userId
        );
        favTeamRepository.delete(favoriteTeam);

        return null;
    }
}
