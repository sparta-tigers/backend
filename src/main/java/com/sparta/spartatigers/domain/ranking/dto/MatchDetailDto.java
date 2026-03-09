package com.sparta.spartatigers.domain.ranking.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.match.model.Match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MatchDetailDto {

	private Long matchId;
	private String postseasonStage;
	private LocalDateTime matchTime;
	private String homeTeamName;
	private String awayTeamName;
	private int homeTeamScore;
	private int awayTeamScore;
	private String stadiumName;
	private String matchResult;

	public static MatchDetailDto from(Match match, String stageName) {
		return MatchDetailDto.builder()
			.matchId(match.getId())
			.postseasonStage(stageName)
			.matchTime(match.getMatchTime())
			.homeTeamName(match.getHomeTeam().getName())
			.awayTeamName(match.getAwayTeam().getName())
			.homeTeamScore(match.getHomeScore()) // 엔티티 필드명에 맞춰 수정
			.awayTeamScore(match.getAwayScore())
			.stadiumName(match.getStadium().getName())
			.matchResult(match.getMatchResult().name())
			.build();
	}
}
