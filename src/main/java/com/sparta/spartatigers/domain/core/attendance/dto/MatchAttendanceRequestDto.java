package com.sparta.spartatigers.domain.core.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MatchAttendanceRequestDto(
    @NotNull(message = "경기 정보는 필수입니다.") Long matchId,
    @NotBlank(message = "좌석 정보는 필수입니다.") String seat,
    String contents,
    List<String> imageUrls
) {}
