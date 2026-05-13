package com.sparta.spartatigers.domain.liveboard.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LiveBoardConnection {
	private String sessionId;
	private Long userId; // null - 비회원
	private String nickname;
	private String roomId;
	private LocalDateTime connectedAt;

	public static LiveBoardConnection of(
		String sessionId,
		Long userId,
		String nickname,
		String roomId,
		LocalDateTime connectedAt) {
		return new LiveBoardConnection(sessionId, userId, nickname, roomId, connectedAt);
	}
}