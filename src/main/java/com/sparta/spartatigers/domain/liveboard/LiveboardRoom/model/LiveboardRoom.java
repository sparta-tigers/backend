package com.sparta.spartatigers.domain.liveboard.LiveboardRoom.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LiveboardRoom {

	private String roomId;
	private Long matchId;
	private String title;
	private LocalDateTime openAt;

	public static LiveboardRoom of (
		String roomId, Long matchId, String title, LocalDateTime openAt) {
		return new LiveboardRoom(roomId, matchId, title, openAt);
	}
}
