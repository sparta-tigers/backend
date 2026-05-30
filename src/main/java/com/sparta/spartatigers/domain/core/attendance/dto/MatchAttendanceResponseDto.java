package com.sparta.spartatigers.domain.core.attendance.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.core.attendance.model.AttendanceImage;
import com.sparta.spartatigers.domain.core.attendance.model.MatchAttendance;

public record MatchAttendanceResponseDto(
		Long id,
		Long matchId,
		String seat,
		String contents,
		List<String> imageUrls,
		LocalDateTime createdAt,
		LocalDateTime matchTime,
		String homeTeamName,
		String awayTeamName,
		String homeTeamCode,
		String awayTeamCode,
		Integer homeScore,
		Integer awayScore,
		String stadiumName) {
	public static MatchAttendanceResponseDto from(MatchAttendance attendance) {
		List<String> urls = attendance.getImages().stream().map(AttendanceImage::getImageUrl)
				.toList();

		return new MatchAttendanceResponseDto(
				attendance.getId(),
				attendance.getMatch().getId(),
				attendance.getSeat(),
				attendance.getContents(),
				urls,
				attendance.getCreatedAt(),
				attendance.getMatch().getMatchTime(),
				attendance.getMatch().getHomeTeam().getName(),
				attendance.getMatch().getAwayTeam().getName(),
				attendance.getMatch().getHomeTeam().getCode().name(),
				attendance.getMatch().getAwayTeam().getCode().name(),
				attendance.getMatch().getHomeScore(),
				attendance.getMatch().getAwayScore(),
				attendance.getMatch().getStadium() != null ? attendance.getMatch().getStadium().getName() : "미지정 구장");
	}
}
