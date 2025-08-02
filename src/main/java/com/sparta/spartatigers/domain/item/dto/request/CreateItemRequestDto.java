package com.sparta.spartatigers.domain.item.dto.request;

import com.sparta.spartatigers.domain.item.model.ItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateItemRequestDto(
    @NotNull(message = "카테고리는 필수입니다.") ItemCategory category,
    @NotBlank(message = "제목은 필수입니다.") String title,
    String seatInfo,
    String description) {}
