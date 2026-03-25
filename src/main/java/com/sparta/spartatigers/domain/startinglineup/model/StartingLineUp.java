package com.sparta.spartatigers.domain.startinglineup.model;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.team.model.TeamCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StartingLineUp {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@Column
	@Enumerated(value = EnumType.STRING)
	private TeamCode teamCode;

	@Column
	private int battingOrder;

	@Column
	private String position; // 현재는 (타자) 또는 (투수) -> 추후에 타자 포지션 넣을 수 있음 여기다 넣기

	@Column
	private String playerName;

	public StartingLineUp(Match match, TeamCode teamCode, int battingOrder, String position, String playerName) {
		this.match = match;
		this.teamCode = teamCode;
		this.battingOrder = battingOrder;
		this.position = position;
		this.playerName = playerName;

	}

	public static StartingLineUp createBatter(Match match, TeamCode teamCode, int battingOrder, String playerName) {
		return new StartingLineUp(match, teamCode, battingOrder, "타자", playerName);
	}

	public static StartingLineUp createPitcher(Match match, TeamCode teamCode,  String playerName) {
		return new StartingLineUp(match, teamCode, 0, "타자", playerName);
	}


}

