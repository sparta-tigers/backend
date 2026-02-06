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

	public List<TeamRankingResponseDto> getRankingByDate(
		LocalDate date
	) {
		LocalDateTime tilltheDay = date.atTime(23,59,59);
		// 해당 일자의 경기 결과를 보고 Stat을 계산
		List<TeamRankingStat> stats =
			rankingRepository.applyTeamRecords(tilltheDay);
		// 스탯을 토대로 랭킹 계산 후 DTO 변환
		return convertToResponse(stats);
	}

	public List<TeamRankingResponseDto> getRankingByYear(
		int year, LeagueType leagueType
	) {
		if(leagueType == LeagueType.POST_SEASON) {
			throw new InvalidRequestException(ExceptionCode.POSTSEASON_RANKING_UNAVAILABLE);
		}
		List<TeamRankingStat> stats =
			rankingRepository.applyTeamRecordsByYear(year, leagueType);
		return convertToResponse(stats);
	}


	private List<TeamRankingResponseDto> convertToResponse(
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
					stat.getLeagueType(),
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