package com.sparta.spartatigers.domain.core.trade.dto.request;

import com.sparta.spartatigers.domain.core.trade.model.ItemCategory;

public record UpdateItemRequestDto(
    ItemCategory category,
    String title,
    String seatInfo,
    String description) {}
