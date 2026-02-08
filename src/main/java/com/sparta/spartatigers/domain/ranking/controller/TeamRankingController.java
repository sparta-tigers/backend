package com.sparta.spartatigers.domain.ranking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.ranking.dto.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.PostSeasonResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.service.TeamRankingService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/rankings")
public class TeamRankingController {

	private final TeamRankingService rankingService;

	/**
	 * [ 날짜별 순위 조회 ]
	 * 특정 날짜까지의 누적 성적을 기반으로 순위를 반환합니다.
	 * @param anyday (default = now)
	 * @return List<TeamRankingResponseDto>
	 */
	@GetMapping("/daily")
	public List<TeamRankingResponseDto> getRankingByDate(
		@RequestParam (required = false)
		@DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday
	) {
		if (anyday == null) {
			anyday = LocalDate.now();
		}
		return rankingService.getRankingByDate(anyday);
	}

	/**
	 * [ 연도별 순위 조회 ]
	 * 특정 연도의 순위를 반환합니다.
	 * @param year (default = currentyear)
	 * @param leagueType
	 * @return
	 */
	@GetMapping("/yearly")
	public List<TeamRankingResponseDto> getRankingByYear(
		@RequestParam (required = false) Integer year,
		@RequestParam LeagueType leagueType
	) {
		if(year == null) {
			year = LocalDate.now().getYear();
		}
		return rankingService.getRankingByYear(year, leagueType);
	}

	@GetMapping("/postseason")
	public PostSeasonResponseDto getPostSeasonMatchesByYear(
		@RequestParam (required = false) Integer year
	) {
		if(year == null) {
			year = LocalDate.now().getYear();
		}
		return rankingService.getPostSeasonResults(year);
	}
}
