package com.sparta.spartatigers.domain.ranking.dto;

import lombok.Getter;

@Getter
public class TeamRankingStat {

	// 내부 계산용 Dto
	private Long teamId;
	private String teamName;

	private int matchCount; // 경기수

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	private TeamRankingStat(Long teamId, String teamName) {
		this.teamId = teamId;
		this.teamName = teamName;
	}

	public static TeamRankingStat from (Long teamId, String teamName) {
		return new TeamRankingStat(teamId, teamName);
	}

	public void recordWin() {
		matchCount++;
		winCount++;
	}

	public void recordLose() {
		matchCount++;
		loseCount++;
	}

	public void recordDraw() {
		matchCount++;
		drawCount++;
	}

	public double getWinRate() {
		int decidedGames = winCount+ loseCount;
		return decidedGames == 0 ? 0.0 : (double) winCount / decidedGames;
	}

}
