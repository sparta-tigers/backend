package com.sparta.spartatigers.domain.item.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// [FIX] 선택적 쿼리 파라미터이므로 @NotNull 제거. 값이 있을 때만 범위 검증.
public record FindItemByIdRequestDto(
    @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
    @Max(value = 90, message = "위도는 90 이하이어야 합니다.")
    Double latitude,

    @Min(value = -180, message = "경도는 -180 이상이어야 합니다.")
    @Max(value = 180, message = "경도는 180 이하이어야 합니다.")
    Double longitude
) {

}
