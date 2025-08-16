package com.sparta.spartatigers.domain.item.dto.request;

import com.sparta.spartatigers.domain.item.model.ItemCategory;

public record UpdateItemRequestDto(
    ItemCategory category,
    String title,
    String seatInfo,
    String description) {}
