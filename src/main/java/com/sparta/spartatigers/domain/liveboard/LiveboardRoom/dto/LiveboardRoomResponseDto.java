package com.sparta.spartatigers.domain.liveboard.LiveboardRoom.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.domain.team.model.TeamCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LiveboardRoomResponseDto {

	private String roomId;
	private String title;
	private Long matchId;
	private String awayTeamName;
	private TeamCode awayTeamCode;
	private String homeTeamName;
	private TeamCode homeTeamCode;
	private LocalDateTime startedAt;
	private MatchResult matchResult;
	private String position;
	private Long connectCount;

}
