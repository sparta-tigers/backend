package com.sparta.spartatigers.domain.liveboardroom.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardStatus;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.team.model.TeamCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LiveBoardRoomResponseDto {

	private String roomId; // null일수 있음
	private Long matchId;
	private String title;
	private LocalDateTime matchTime;

	// private double temperature; //기온(TMP) - 초단기실황, 초단기예보
	// private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보, 단기예보

	private LiveBoardStatus liveBoardStatus;

	private String awayTeamName;
	private TeamCode awayTeamCode; // 이미지
	private String homeTeamName;
	private TeamCode homeTeamCode; // 이미지
	private MatchResult matchResult;
	private String stadium;
	private Long connectCount;
	@JsonProperty("isTodayMatch")
	private boolean isTodayMatch; // TODO : 룸이 있는지 없는지 검증용?


	public static LiveBoardRoomResponseDto fromUpcomingMatch(Match match) {
		return LiveBoardRoomResponseDto.builder()
			.roomId(null)
			.matchId(match.getId())
			.title(match.getAwayTeam().getName()+"VS"+match.getHomeTeam().getName())
			.matchTime(match.getMatchTime())
			.liveBoardStatus(LiveBoardStatus.UPCOMING)
			.awayTeamName(match.getAwayTeam().getName())
			.awayTeamCode(match.getAwayTeam().getCode())
			.homeTeamName(match.getHomeTeam().getName())
			.homeTeamCode(match.getHomeTeam().getCode())
			.matchResult(match.getMatchResult()) // 없을수있음
			.stadium(match.getStadium() != null ? match.getStadium().getName() : null)
			.connectCount(0L)
			.isTodayMatch(false)
			.build();
	}

	public static LiveBoardRoomResponseDto fromTodayMatch(Match match, LiveBoardRoom room, long connectCount) {
		return LiveBoardRoomResponseDto.builder()
			.roomId(room.getRoomId())
			.matchId(match.getId())
			.title(room.getTitle())
			.matchTime(match.getMatchTime())
			.liveBoardStatus(LiveBoardStatus.TODAY)
			.awayTeamName(match.getAwayTeam().getName())
			.awayTeamCode(match.getAwayTeam().getCode())
			.homeTeamName(match.getHomeTeam().getName())
			.homeTeamCode(match.getHomeTeam().getCode())
			.matchResult(match.getMatchResult()) // 없을수있음
			.stadium(match.getStadium() != null ? match.getStadium().getName() : null)
			.connectCount(connectCount)
			.isTodayMatch(true)
			.build();
	}

	public static LiveBoardRoomResponseDto fromPastMatch(Match match, LiveBoardRoom room) {
		return LiveBoardRoomResponseDto.builder()
			.roomId(room.getRoomId())
			.matchId(match.getId())
			.title(room.getTitle())
			.matchTime(match.getMatchTime())
			.liveBoardStatus(LiveBoardStatus.PAST)
			.awayTeamName(match.getAwayTeam().getName())
			.awayTeamCode(match.getAwayTeam().getCode())
			.homeTeamName(match.getHomeTeam().getName())
			.homeTeamCode(match.getHomeTeam().getCode())
			.matchResult(match.getMatchResult()) // 없을수없음
			.stadium(match.getStadium() != null ? match.getStadium().getName() : null)
			.connectCount(0L)
			.isTodayMatch(false)
			.build();
	}

}
