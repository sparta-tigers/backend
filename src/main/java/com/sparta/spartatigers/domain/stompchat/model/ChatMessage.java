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
	private String favTeamSymbolUrl;

	public static ChatMessage ofLiveBoardRoom (
		String roomId, Long senderId, String senderNickname, String content, String favTeamSymbolUrl, LocalDateTime sentAt
	) {
		return new ChatMessage(roomId, senderId, senderNickname, content, sentAt, ChatDomainType.LIVEBOARD, favTeamSymbolUrl);
	}
}
