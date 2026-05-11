package com.sparta.spartatigers.domain.liveboard.matchAttendance.dto;

import java.util.List;

public record MatchAttendanceUpdateRequestDto(
	String seat,
	String contents,
	List<String> oldImageUrls // 기존 이미지 주소들임 새 이미지는 파일로
) {
}
