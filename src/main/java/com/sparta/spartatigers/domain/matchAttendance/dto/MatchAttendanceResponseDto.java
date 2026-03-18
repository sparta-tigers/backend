package com.sparta.spartatigers.domain.matchAttendance.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.matchAttendance.model.AttendanceImage;
import com.sparta.spartatigers.domain.matchAttendance.model.MatchAttendance;

public record MatchAttendanceResponseDto (

	Long id,
	Long matchId,
	String seat,
	String contents,
	List<String> imageUrls,
	LocalDateTime createdAt
)
{
	public static MatchAttendanceResponseDto from(MatchAttendance attendance) {
		List<String> urls = attendance.getImages().stream().map(AttendanceImage::getImageUrl)
			.toList();

		return new MatchAttendanceResponseDto(
			attendance.getId(),
			attendance.getMatch().getId(),
			attendance.getSeat(),
			attendance.getContents(),
			urls,
			attendance.getCreatedAt()
		);
	}
}
