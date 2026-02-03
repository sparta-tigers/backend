package com.sparta.spartatigers.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamRankingResponseDto {

	// 프론트 응답용 dto
	private int rank;
	private Long teamId;
	private String teamName;

	private int matchCount; // 경기수

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	private double winRate; // 승률


	public static TeamRankingResponseDto of(
		int rank, Long teamId, String teamName, int matchCount, int winCount, int loseCount, int drawCount,
		double winRate
	) {
		return new TeamRankingResponseDto(
			rank,
			teamId,
			teamName,
			matchCount,
			winCount,
			loseCount,
			drawCount,
			winRate
		);
	}
}
