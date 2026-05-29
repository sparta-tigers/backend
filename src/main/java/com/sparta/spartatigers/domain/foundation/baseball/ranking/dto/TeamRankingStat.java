package com.sparta.spartatigers.domain.foundation.baseball.ranking.dto;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.LeagueType;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.TeamCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamRankingStat {

	// 내부 계산용 Dto
	private LeagueType leagueType;
	private Long teamId;
	private String teamName;
	private TeamCode teamCode;

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	public int getMatchCount() {
		return winCount + loseCount + drawCount;
	}
}
