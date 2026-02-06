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
	 * [ 날짜별 KBO 구단 순위 조회 ]
	 * 특정 일자의 구단 순위 전체와, 해당 날짜가 속하는 리그 타입을 반환합니다.
	 * @param anyday
	 * @return List<TeamRankingResponseDto>
	 */
	@GetMapping
	public List<TeamRankingResponseDto> getRankingByDate(
		@RequestParam (required = false)
		@DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday
	) {
		if (anyday == null) {
			anyday = LocalDate.now();
		}
		return rankingService.getTeamRanking(anyday);
	}

}
