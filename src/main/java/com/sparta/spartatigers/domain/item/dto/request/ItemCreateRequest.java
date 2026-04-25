package com.sparta.spartatigers.domain.item.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.spartatigers.domain.item.model.ItemCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// [FIX] ignoreUnknown = false: 클라이언트 오타 필드(예: desiredItems)를 조용히 무시하지 않고
// JsonProcessingException을 발생시켜 컨트롤러에서 VALIDATION_ERROR로 처리하도록 강제
@JsonIgnoreProperties(ignoreUnknown = false)
public record ItemCreateRequest(
    @NotNull(message = "카테고리는 필수입니다.") ItemCategory category,
    @NotBlank(message = "제목은 필수입니다.") String title,
    String seatInfo,
    String description,
    String desiredItem, // 기획서 상의 WANT 스펙 복구
    Location location
) {

    public record Location(
        // [FIX] 위도(latitude): 지구 좌표계 기준 -90 ~ +90
        @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
        @Max(value = 90, message = "위도는 90 이하이어야 합니다.")
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        // [FIX] 경도(longitude): 지구 좌표계 기준 -180 ~ +180
        @Min(value = -180, message = "경도는 -180 이상이어야 합니다.")
        @Max(value = 180, message = "경도는 180 이하이어야 합니다.")
        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        String address
    ) {
    }
}
