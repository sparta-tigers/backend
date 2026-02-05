package com.sparta.spartatigers.domain.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.ranking.dto.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;
import com.sparta.spartatigers.domain.ranking.repository.TeamRankingRepositoryCustom;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamRankingService {

	private final TeamRankingRepositoryCustom rankingRepository;

	public List<TeamRankingResponseDto> getTeamRanking(
		LeagueType leagueType,
		LocalDate date
	) {
		// 파라미터 date가 어떤 리그에 속하는지
		LocalDateTime from = findleagueSchedule(leagueType);
		LocalDateTime to = date.atTime(23,59,59);

		// 해당 일자의 경기 결과를 보고 Stat을 계산
		List<TeamRankingStat> stats =
			rankingRepository.applyTeamRecords(from, to);

		// 스탯을 토대로 랭킹 계산 후 DTO 변환
		return convertToResponse(leagueType, stats);
	}

	// TODO : 테스트 용으로 리그 일정 하드코딩 중
	private LocalDateTime findleagueSchedule(LeagueType leagueType) {
		return switch (leagueType) {
			case PRESEASON -> LocalDate.of(2025,2,1).atStartOfDay();
			case REGULAR -> LocalDate.of(2025,3,22).atStartOfDay();
			default -> throw new InvalidRequestException(ExceptionCode.LEAGUE_NOT_FOUND);
		};
	}

	private List<TeamRankingResponseDto> convertToResponse(
		LeagueType leagueType,
		List<TeamRankingStat> stats
	) {

		// Stat 승률 기준 내림차순 정렬
		List<TeamRankingStat> sorted = stats.stream().sorted(
			Comparator.comparingDouble(this::winRate).reversed()
		).toList();

		List<TeamRankingResponseDto> result = new ArrayList<>();

		int currentRank = 0;
		int displayRank = 0;
		double prevWinRate = -1.0;

		for (TeamRankingStat stat : sorted) {
			currentRank++;
			double winRate = winRate(stat);

			// 공동 순위는 같은 등수로!
			if (Double.compare(winRate, prevWinRate) != 0) {
				displayRank = currentRank;
				prevWinRate = winRate;
			}

			result.add(TeamRankingResponseDto.of(
					leagueType,
					displayRank,
					stat.getTeamId(),
					stat.getTeamName(),
					stat.getMatchCount(),
					stat.getWinCount(),
					stat.getLoseCount(),
					stat.getDrawCount(),
					winRate
				)
			);
		}
		return result;
	}

	private double winRate(TeamRankingStat stat) {
		int win = stat.getWinCount();
		int lose = stat.getLoseCount();

		if (win + lose == 0) {
			return 0.0;
		}
		return (double) win / (win +lose);
	}

}