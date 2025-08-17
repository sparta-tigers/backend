package com.sparta.spartatigers.domain.liveboardroom.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.team.model.TeamCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LiveBoardRoomResponseDto {

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

	public static LiveBoardRoomResponseDto of(LiveBoardRoom room, long connectCount) {
		return builder()
			.roomId(room.getRoomId())
			.title(room.getTitle())
			.startedAt(room.getOpenAt())
			.connectCount(connectCount)
			.build();
		// return new LiveBoardRoomResponseDto(
		//         room.getRoomId(), room.getTitle(), room.getOpenAt(), connectCount);
	}
}
