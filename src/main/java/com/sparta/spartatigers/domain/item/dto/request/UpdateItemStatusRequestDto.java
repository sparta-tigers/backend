package com.sparta.spartatigers.domain.item.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateItemStatusRequestDto(
    @NotNull ItemAction action
) {}
