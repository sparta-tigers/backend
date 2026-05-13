package com.sparta.spartatigers.domain.ranking.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.liveboard.model.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.PostSeasonResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.service.TeamRankingService;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/** [ 팀 순위 및 포스트시즌 정보 API 컨트롤러]
 *
 * 일자별 / 년도별 정규리그 순위와 포스트 시즌 경기 결과를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class TeamRankingController {

	private final TeamRankingService rankingService;
	private final Clock clock;

	/**
	 * [ 일자별 순위 조회 ]
	 * 특정 날짜까지의 누적 경기 기록을 바탕으로 산출된 순위를 반환합니다.
	 * 날짜를 입력하지 않으면 오늘 날짜를 기준으로 조회합니다.
	 *
	 * @param anyday 조회 기준 날짜 (포맷 : yyyyMMdd, Default : 오늘)
	 * @return 순위대로 정렬된 팀 랭킹 리스트
	 */
	@GetMapping("/daily")
	public ApiResponse<List<TeamRankingResponseDto>> getRankingByDate(
		@RequestParam (required = false)
		@DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday,
		@RequestParam LeagueType leagueType
	) {
		if (anyday == null) {
			anyday = LocalDate.now(clock);
		}
		return ApiResponse.success(rankingService.getRankingByDate(anyday, leagueType));
	}

	/**
	 * [ 연도별 순위 조회 ]
	 * 특정 연도의 정규리그 또는 시범경기 최종 순위를 조회합니다.
	 * 포스트 시즌은 순위 집계 대상이 아니므로 조회할 수 없습니다.
	 *
	 * @param year 조회할 시즌 연도 (Default = 현재 연도)
	 * @param leagueType 리그 타입 (PRESEASON 시범경기, REGULAR 정규 리그)
	 * @return 해당 시즌의 최종 팀 랭킹 리스트
	 */
	@GetMapping("/yearly")
	public ApiResponse<List<TeamRankingResponseDto>> getRankingByYear(
		@RequestParam (required = false) Integer year,
		@RequestParam LeagueType leagueType
	) {
		if(year == null) {
			year = LocalDate.now(clock).getYear();
		}
		return ApiResponse.success(rankingService.getRankingByYear(year, leagueType));
	}

	/**
	 * [ 포스트 시즌 결과 조회 ]
	 * 특정 연도의 포스트 시즌 대진표와 경기결과(와일드카드~한국시리즈)를 조회합니다.
	 * @param year 조회할 시즌 연도 (Default : 현재 연도)
	 * @return 단계별 경기 목록 및 우승팀 정보
	 */
	@GetMapping("/postseason")
	public ApiResponse<PostSeasonResponseDto> getPostSeasonMatchesByYear(
		@RequestParam (required = false) Integer year
	) {
		if(year == null) {
			year = LocalDate.now(clock).getYear();
		}
		return ApiResponse.success(rankingService.getPostSeasonResults(year));
	}
}
