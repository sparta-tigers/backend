package com.sparta.spartatigers.domain.item.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemCategory;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.item.service.ItemService;
import com.sparta.spartatigers.domain.user.dto.UserResponseDto;

public record ReadItemResponseDto(
    Long id,
    UserResponseDto user,
    ItemCategory category,
    String title,
    ItemStatus status,
    List<String> imageUrls,
    Double latitude,
    Double longitude,
    String address,
    LocalDateTime createdAt) {

    public static ReadItemResponseDto from(Item item, ItemService itemService) {
        // JSON 문자열을 List<String>으로 역직렬화
        List<String> imageUrls = itemService.deserializeImageUrls(item.getImage());

        return new ReadItemResponseDto(
            item.getId(),
            UserResponseDto.from(item.getUser()),
            item.getCategory(),
            item.getTitle(),
            item.getStatus(),
            imageUrls,
            item.getLatitude(),
            item.getLongitude(),
            item.getAddress(),
            item.getCreatedAt());
    }
    
    // 기존 호환성을 위한 오버로드 (이미지 없는 경우)
    public static ReadItemResponseDto from(Item item) {
        // itemService가 null이면 빈 리스트로 처리
        List<String> imageUrls = List.of();
        
        return new ReadItemResponseDto(
            item.getId(),
            UserResponseDto.from(item.getUser()),
            item.getCategory(),
            item.getTitle(),
            item.getStatus(),
            imageUrls,
            item.getLatitude(),
            item.getLongitude(),
            item.getAddress(),
            item.getCreatedAt());
    }
}
