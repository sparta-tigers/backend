package com.sparta.spartatigers.domain.liveboardroom.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LiveBoardRoom {

	private String roomId;
	private Long matchId;
	private String title;
	private LocalDateTime openAt;

	public static LiveBoardRoom of (
		String roomId, Long matchId, String title, LocalDateTime openAt) {
		return new LiveBoardRoom(roomId, matchId, title, openAt);
	}
}
