package com.sparta.spartatigers.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamRankingStat {

	// 내부 계산용 Dto
	private Long teamId;
	private String teamName;

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	public int getMatchCount() {
		return winCount + loseCount + drawCount;
	}
}
