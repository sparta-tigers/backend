package com.sparta.spartatigers.domain.matchAttendance.dto;

import java.util.List;

public record MatchAttendanceUpdateRequestDto(
	String seat,
	String contents,
	List<String> imageUrls
) {
}
