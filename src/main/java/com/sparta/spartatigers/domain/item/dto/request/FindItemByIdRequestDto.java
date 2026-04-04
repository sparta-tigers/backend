package com.sparta.spartatigers.domain.item.dto.request;

import jakarta.validation.constraints.NotNull;

public record FindItemByIdRequestDto(
    @NotNull Double latitude,
    @NotNull Double longitude
) {

}
