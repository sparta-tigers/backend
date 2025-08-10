package com.sparta.spartatigers.domain.stompchat.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

	private String roomId;
	private Long senderId;
	private String senderNickname;
	private String content;
	private LocalDateTime sentAt;
	private ChatDomainType domain;

	public static ChatMessage ofExchangeRoom (
		String roomId, Long senderId, String senderNickname, String content
	) {
		return new ChatMessage(roomId, senderId, senderNickname, content, LocalDateTime.now(), ChatDomainType.EXCHANGE);
	}

	public static ChatMessage ofLiveBoardRoom (
		String roomId, Long senderId,String senderNickname, String content
	) {
		return new ChatMessage(roomId, senderId, senderNickname, content, LocalDateTime.now(), ChatDomainType.LIVEBOARD);
	}
}
