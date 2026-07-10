package com.sparta.spartatigers.domain.core.attendance.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MatchAttendanceRequestDto(
        @NotNull(message = "경기 정보는 필수입니다.") Long matchId,
        @NotBlank(message = "좌석 정보는 필수입니다.") String seat,
        String contents,
        List<String> imageUrls

) {

}
