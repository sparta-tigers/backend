package com.sparta.spartatigers.domain.item.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

// [FIX] 문제 5: Double 타입은 오차로 인해 @Min/@Max가 공식 지원되지 않음.
// 정밀한 수치 검증을 위해 BigDecimal과 @DecimalMin/@DecimalMax로 변경.
public record FindItemByIdRequestDto(
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
    BigDecimal latitude,

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
    BigDecimal longitude
) {

}
