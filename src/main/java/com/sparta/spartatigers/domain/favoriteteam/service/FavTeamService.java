package com.sparta.spartatigers.domain.favoriteteam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.favoriteteam.dto.FavTeamRequestDto;
import com.sparta.spartatigers.domain.favoriteteam.dto.FavTeamResponseDto;
import com.sparta.spartatigers.domain.favoriteteam.model.entity.FavoriteTeam;
import com.sparta.spartatigers.domain.favoriteteam.repository.FavTeamRepository;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.domain.team.repository.TeamRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavTeamService {
	private final FavTeamRepository favTeamRepository;
	private final UserRepository userRepository;
	private final TeamRepository teamRepository;

	@Transactional
	public FavTeamResponseDto add (FavTeamRequestDto request, Long userId) {
		User user = userRepository.findByIdOrElseThrow(userId);
		boolean alreadyRegistered = favTeamRepository.existsByUser(user);
		if(alreadyRegistered) {
			throw new InvalidRequestException(ExceptionCode.ALREADY_EXISTS_FAVORITE_TEAM);
		}
		Team team = teamRepository.findByIdOrElseThrow(request.getTeamId());
		FavoriteTeam favoriteTeam = FavoriteTeam.from(user, team);
		favTeamRepository.save(favoriteTeam);
		return FavTeamResponseDto.of(favoriteTeam);
	}

	@Transactional
	public FavTeamResponseDto get(Long userId) {
		FavoriteTeam favoriteTeam = favTeamRepository.findByUserIdOrElseThrow(userId);

		return FavTeamResponseDto.of(favoriteTeam);
	}

	@Transactional
	public FavTeamResponseDto update(FavTeamRequestDto request, Long userId) {
		FavoriteTeam favoriteTeam = favTeamRepository.findByUserIdOrElseThrow(userId);
		Team newTeam = teamRepository.findByIdOrElseThrow(request.getTeamId());
		favoriteTeam.update(newTeam);
		return FavTeamResponseDto.of(favoriteTeam);
	}

	@Transactional
	public Void delete(Long userId) {
		userRepository.findByIdOrElseThrow(userId);
		FavoriteTeam favoriteTeam = favTeamRepository.findByUserIdOrElseThrow(userId);
		favTeamRepository.delete(favoriteTeam);

		return null;
	}


}
