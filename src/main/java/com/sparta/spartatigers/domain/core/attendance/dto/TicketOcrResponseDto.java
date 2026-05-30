package com.sparta.spartatigers.domain.core.attendance.dto;

public record TicketOcrResponseDto(
		String imageUrl,
		String seatInfo) {
	public static TicketOcrResponseDto from(String imageUrl, String seatInfo) {
		return new TicketOcrResponseDto(
				imageUrl, seatInfo);
	}
}
