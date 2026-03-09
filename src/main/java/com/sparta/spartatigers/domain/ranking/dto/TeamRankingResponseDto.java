package com.sparta.spartatigers.domain.ranking.dto;

import com.sparta.spartatigers.domain.match.model.LeagueType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamRankingResponseDto {

	// 프론트 응답용 dto
	private LeagueType leagueType;

	private int rank;
	private Long teamId;
	private String teamName;

	private int matchCount; // 경기수 ( 취소 / 미경기 제외 )

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	private double winRate; // 승률 = win / (win+lose)


	public static TeamRankingResponseDto of(
		LeagueType leagueType, int rank, Long teamId, String teamName, int matchCount, int winCount, int loseCount, int drawCount,
		double winRate
	) {
		return new TeamRankingResponseDto(
			leagueType,
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
