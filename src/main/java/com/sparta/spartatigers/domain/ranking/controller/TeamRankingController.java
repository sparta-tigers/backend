package com.sparta.spartatigers.domain.ranking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.ranking.dto.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.service.TeamRankingService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/rankings")
public class TeamRankingController {

	private final TeamRankingService rankingService;

	/**
	 * 특정 일자까지의 리그 전체 순위를 리턴합니다.
	 * @param leagueType - PRESEASON, REGULAR, POST_SEASON
	 * @param anyday
	 * @return 해당 리그의 전체 순위를 리턴합니다.
	 */
	@GetMapping
	public List<TeamRankingResponseDto> getRankingByDate(
		@RequestParam LeagueType leagueType,
		@RequestParam (required = false)
		@DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday
	) {
		if (anyday == null) {
			anyday = LocalDate.now();
		}
		return rankingService.getTeamRanking(leagueType, anyday);
	}

}
