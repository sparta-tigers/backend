package com.sparta.spartatigers.domain.item.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.spartatigers.domain.item.model.ItemCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드 무시 (500 에러 방어)
public record ItemCreateRequest(
    @NotNull(message = "카테고리는 필수입니다.") ItemCategory category,
    @NotBlank(message = "제목은 필수입니다.") String title,
    String seatInfo,
    String description,
    String desiredItem, // 기획서 상의 WANT 스펙 복구
    Location location
) {

    public record Location(
        @Min(value = -180, message = "위도는 -180 이상이어야 합니다.")
        @Max(value = 180, message = "위도는 180 이하이어야 합니다.")
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @Min(value = -90, message = "경도는 -90 이상이어야 합니다.")
        @Max(value = 90, message = "경도는 90 이하이어야 합니다.")
        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        String address
    ) {
    }
}
