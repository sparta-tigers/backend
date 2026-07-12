package com.sparta.spartatigers.domain.core.trade.dto.response;

import com.sparta.spartatigers.domain.core.trade.model.Item;
import com.sparta.spartatigers.domain.core.trade.model.ItemCategory;
import com.sparta.spartatigers.domain.core.trade.model.ItemStatus;
import com.sparta.spartatigers.domain.core.trade.service.ItemService;
import com.sparta.spartatigers.domain.foundation.user.account.dto.UserResponseDto;
import java.time.LocalDateTime;
import java.util.List;

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
    LocalDateTime createdAt
) {
    public static ReadItemResponseDto from(Item item, ItemService itemService) {
        // JSON 문자열을 List<String>으로 역직렬화
        List<String> imageUrls = itemService.deserializeImageUrls(
            item.getImage()
        );

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
            item.getCreatedAt()
        );
    }

    /**
     * [이미지 미포함 응답 전용] — 이미지 URL 없이 아이템 정보만 반환합니다.
     *
     * <p>이 팩토리는 imageUrls를 항상 빈 리스트({@code List.of()})로 반환합니다.
     * "이미지가 없는 아이템"과 "이미지를 의도적으로 비운 응답"을 구분하기 어려우므로,
     * 이미지 URL이 필요한 경우에는 {@link #from(Item, ItemService)}를 사용해야 합니다.</p>
     *
     * <p>현재 사용처: WebSocket 이벤트(ItemLocationUpdatedEvent) 페이로드 — 이미지 없이 위치/상태만 전달.</p>
     */
    public static ReadItemResponseDto from(Item item) {
        return new ReadItemResponseDto(
            item.getId(),
            UserResponseDto.from(item.getUser()),
            item.getCategory(),
            item.getTitle(),
            item.getStatus(),
            List.of(), // 의도적으로 빈 리스트: 이미지 URL 미포함 응답 전용
            item.getLatitude(),
            item.getLongitude(),
            item.getAddress(),
            item.getCreatedAt()
        );
    }
}
