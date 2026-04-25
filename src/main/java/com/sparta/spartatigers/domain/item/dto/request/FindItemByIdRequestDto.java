package com.sparta.spartatigers.domain.item.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// [FIX] @NotNull만 있어 범위를 벗어난 좌표가 Haversine 식으로 전달되는 문제 수정
// ItemCreateRequest.Location과 동일한 검증 정책 적용
public record FindItemByIdRequestDto(
    @NotNull(message = "위도는 필수입니다.")
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
    @Max(value = 90, message = "위도는 90 이하이어야 합니다.")
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    @Min(value = -180, message = "경도는 -180 이상이어야 합니다.")
    @Max(value = 180, message = "경도는 180 이하이어야 합니다.")
    Double longitude
) {

}
