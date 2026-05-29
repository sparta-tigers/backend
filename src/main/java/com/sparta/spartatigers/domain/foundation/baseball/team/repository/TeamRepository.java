package com.sparta.spartatigers.domain.foundation.baseball.team.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.foundation.baseball.team.model.Team;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.TeamCode;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

public interface TeamRepository extends JpaRepository<Team, Long> {

	Optional<Team> findByCode(TeamCode code);

	default Team findByIdOrElseThrow(Long teamId) {
		return findById(teamId).orElseThrow(()->new InvalidRequestException(ExceptionCode.TEAM_NOT_FOUND));
	}

	default Team findByCodeOrElseThrow(TeamCode code) {
		return findByCode(code).orElseThrow(()->new InvalidRequestException(ExceptionCode.TEAM_NOT_FOUND));
	}
}
