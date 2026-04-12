package com.sparta.spartatigers.domain.startinglineup.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class StartingLineupPK implements Serializable {

	@Column(name = "match_id")
	private Long matchId;

	@Column(name = "team_id")
	private Long teamId;

	private StartingLineupPK(Long matchId, Long teamId) {
		this.matchId = matchId;
		this.teamId = teamId;
	}

	public static StartingLineupPK of(Long matchId, Long teamId) {
		return new StartingLineupPK(matchId, teamId);
	}
}