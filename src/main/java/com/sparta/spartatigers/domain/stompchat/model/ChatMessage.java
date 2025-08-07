package com.sparta.spartatigers.domain.stompchat.model;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ChatMessage {

	private String roomId;
	private Long senderId;
	private String content;
	private LocalDateTime sentAt;
	private ChatDomainType domain;
	private MessageType type;
}
