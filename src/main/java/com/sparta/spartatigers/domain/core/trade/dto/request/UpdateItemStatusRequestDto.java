package com.sparta.spartatigers.domain.core.trade.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateItemStatusRequestDto(
    @NotNull ItemAction action
) {}
