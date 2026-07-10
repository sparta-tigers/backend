package com.sparta.spartatigers.domain.foundation.baseball.ranking.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostSeasonResponseDto {

	private int seasonYear;
	private String champion; // 한국시리즈 우승팀
	private Map<PostseasonStage, List<MatchDetailDto>> postSeasonMatches;

	public static PostSeasonResponseDto from(int seasonYear, String champion,
			Map<PostseasonStage, List<MatchDetailDto>> classifiedMatches) {
		return new PostSeasonResponseDto(
				seasonYear,
				champion,
				classifiedMatches);
	}
}
