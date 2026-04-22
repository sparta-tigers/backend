package com.sparta.spartatigers.domain.startinglineup.model;

import java.util.ArrayList;
import java.util.List;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.team.model.Team;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StartingLineup {

	@EmbeddedId
	private StartingLineupPK id; // match id + team Id 로 구분했으면 좋겠어서 복합키 사용

	@MapsId("matchId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id")
	private Match match;

	@MapsId("teamId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id")
	private Team team;

	@OneToMany(mappedBy = "startingLineup", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LineupPlayer> lineupPlayers = new ArrayList<>();

	private String startingPitcher;

}

