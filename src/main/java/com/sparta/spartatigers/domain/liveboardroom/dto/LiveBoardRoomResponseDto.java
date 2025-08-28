package com.sparta.spartatigers.domain.liveboardroom.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardStatus;
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
	private Long matchId;
	private String title;
	private LocalDateTime matchTime;

	private LiveBoardStatus liveBoardStatus;

	private String awayTeamName;
	private TeamCode awayTeamCode;
	private String homeTeamName;
	private TeamCode homeTeamCode;
	private MatchResult matchResult;
	private String stadium;

	private String position; // TODO : 지워도 되나용? 여쭤보기

	private Long connectCount;
	private boolean isTodayMatch; //-> 경기 당일인지 전후인지만 표현하면됨 취소는 matchresult에서


	public static LiveBoardRoomResponseDto of(LiveBoardRoom room, long connectCount) {
		return builder()
			.roomId(room.getRoomId())
			.title(room.getTitle())
			.matchTime(room.getMatchTime())
			.connectCount(connectCount)
			.build();
		// return new LiveBoardRoomResponseDto(
		//         room.getRoomId(), room.getTitle(), room.getOpenAt(), connectCount);
	}
}
