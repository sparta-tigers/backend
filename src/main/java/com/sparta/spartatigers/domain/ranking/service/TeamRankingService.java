package com.sparta.spartatigers.domain.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.ranking.dto.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.MatchDetailDto;
import com.sparta.spartatigers.domain.ranking.dto.PostSeasonResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.PostseasonStage;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;
import com.sparta.spartatigers.domain.ranking.repository.TeamRankingRepositoryCustom;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRankingService {

	private final TeamRankingRepositoryCustom rankingRepository;

	// --- Public  ---

	/**
	 * 특정 날짜 기준 순위 산출
	 * 해당 날짜의 23:59:59 까지 종료된 모든 경기 결과를 합산하여 순위를 매깁니다.
	 * 해당 날짜로부터 가장 최근 진행된 리그의 순위를 계산합니다.
	 *
	 * @param date 기준 날짜
	 * @return 승률 내림차 순으로 정렬된 랭킹 리스트
	 */
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

	/**
	 * 연도별 시즌 최종 순위 산출
	 * 지정된 연도의 전체 시즌 데이터를 집계합니다.
	 * 단, 포스트시즌은 순위 집계 대상이 아니므로 예외를 발생시킵니다.
	 *
	 * @param year 시즌 연도
	 * @param leagueType 리그 타입 (REGULAR, EXHIBITION)
	 * @return 승률 내림차순으로 정렬된 순위 리스트
	 * @throws InvalidRequestException POST_SEASON 타입으로 요청 시 발생
	 */
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

	/**
	 * 포스트 시즌 전체 결과 조회
	 * 해당 연도의 포스트시즌 경기를 단계별(와일드카드 -> 한국시리즈)로 분류하고,
	 * 각 단계의 경기 상세 정보와 최종 우승팀을 반환합니다.
	 *
	 * @param year 시즌 연도
	 * @return 포스트 시즌 단계별 경기 목록 및 우승팀 정보
	 */
	public PostSeasonResponseDto getPostSeasonResults(
		int year
	) {
		Map<PostseasonStage, List<Match>> classifyStages = rankingRepository.classifyStages(year);

		Map<PostseasonStage, List<MatchDetailDto>> postSeasonMatches = new LinkedHashMap<>();

		classifyStages.forEach((postseasonStage, matches) -> {
			List<MatchDetailDto> dtos = new ArrayList<>();
			for (int i = 0; i < matches.size(); i++) {
				String stageLabel = postseasonStage.getKrName() + " " + (i + 1) + "차전";
				dtos.add(MatchDetailDto.from(matches.get(i), stageLabel));
			}
			postSeasonMatches.put(postseasonStage, dtos);
		});

		String champion = extractChampion(classifyStages.get(PostseasonStage.KOREAN_SERIES));
		return PostSeasonResponseDto.from(year, champion, postSeasonMatches);
	}

	// --- Internal Helpers (Private) ---

	/** 한국시리즈 4승 선승제 기반 우승팀 판별 */
	private String extractChampion(List<Match> postSeasonMatches) {
		if(postSeasonMatches == null || postSeasonMatches.isEmpty()) return "한국시리즈 진출팀 결정 전";

		Map<String, Integer> winCountMap = new HashMap<>();
		int targetWins = 4;

		for(Match match : postSeasonMatches) {
			String winner = null;
			if(match.getMatchResult() == MatchResult.HOME_WIN) {
				winner = match.getHomeTeam().getName();
			} else if (match.getMatchResult() == MatchResult.AWAY_WIN) {
				winner = match.getAwayTeam().getName();
			}
			if(winner != null) {
				int currentWins = winCountMap.getOrDefault(winner,0) +1;
				winCountMap.put(winner, currentWins);
				if(currentWins >= targetWins) {
					return winner;
				}
			}
		}
		return "진행 중";
	}

	/** 집계 데이터를 응답 DTO로 변환 + 순위 부여 */
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

	/** 승률 계산 (승 / 승 + 패) */
	private double winRate(TeamRankingStat stat) {
		int win = stat.getWinCount();
		int lose = stat.getLoseCount();

		if (win + lose == 0) {
			return 0.0;
		}
		return (double) win / (win +lose);
	}

}