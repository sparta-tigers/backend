package com.sparta.spartatigers.domain.matchAttendance.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record MatchAttendanceRequestDto(
	@NotBlank(message = "경기 정보는 필수입니다.")
	Long matchId,
	@NotBlank(message = "좌석 정보는 필수입니다.")
	String seat,
	String contents,
	List<String> imageUrls

) {


}
