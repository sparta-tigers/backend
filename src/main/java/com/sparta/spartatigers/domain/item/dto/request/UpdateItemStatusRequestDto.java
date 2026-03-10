package com.sparta.spartatigers.domain.item.dto.request;

import com.sparta.spartatigers.domain.item.model.ItemStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateItemStatusRequestDto(
    @NotNull ItemStatus status
) {}
