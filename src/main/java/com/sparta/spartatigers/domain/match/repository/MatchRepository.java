package com.sparta.spartatigers.domain.match.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.match.model.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

	// 날짜별 조회
	@Query(
		"""
		SELECT m
		FROM matches m
		JOIN FETCH m.homeTeam
		JOIN FETCH m.awayTeam
		LEFT JOIN FETCH m.stadium
		WHERE m.matchTime >= :start AND m.matchTime < :end
		""")
	List<Match> findAllByMatchTimeBetween(LocalDateTime start, LocalDateTime end);

	List<Match> findAllByIdIn(Set<Long> ids);

	@Query(
		"""
		SELECT m
		FROM matches m
		JOIN FETCH m.homeTeam
		JOIN FETCH m.awayTeam
		JOIN FETCH m.stadium
		WHERE m.id = :matchId
		""")
	Optional<Match> findByMatchId(Long matchId) ;

	// [예매알림] 홈시리즈의 첫번째 경기 찾기
	@Query(
		"""
		SELECT m
		FROM matches m
		WHERE m.awayTeam.id = :awayTeamId
				AND m.homeTeam.id = :homeTeamId
				AND m.matchTime BETWEEN :from AND :to
		ORDER BY m.matchTime ASC LIMIT 1
		"""
	)
	Optional<Match> findFirstHomeSeriesMatch (Long awayTeamId, Long homeTeamId, LocalDateTime from, LocalDateTime to);


}
