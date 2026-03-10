package com.sparta.spartatigers.domain.item.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.item.dto.request.ItemCreateRequest;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.stompchat.service.LocationService;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final double SEARCH_RADIUS_KM = 0.05;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;

    private final ObjectMapper objectMapper;
    
    @Transactional
    public ItemResponseDto createItemWithImages(ItemCreateRequest request, TokenClaim tokenClaim, List<String> imageUrls) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        // 이미지 URL 리스트를 JSON으로 안전하게 직렬화
        String imageUrlsJson = serializeImageUrls(imageUrls);

        Item item = new Item(
            request.category(),
            imageUrlsJson,
            request.seatInfo(),
            request.title(),
            request.description(),
            ItemStatus.REGISTERED,
            user,
            LocalDate.now()
        );
        
        itemRepository.save(item);

        ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item, this);
        locationService.notifyUsersNearBy(user.getId(), "ADD_ITEM", newItemDto);

        return ItemResponseDto.from(item);
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findAllItems(TokenClaim tokenClaim, Pageable pageable) {

        Long userId = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR)).getId();

        List<Long> nearByUserIds = locationService.findUsersNearBy(userId, SEARCH_RADIUS_KM);
        nearByUserIds.add(userId);

        Page<Item> itemList = itemRepository.findAllItems(ItemStatus.REGISTERED, LocalDate.now(), nearByUserIds, pageable);

        return itemList.map(item -> ReadItemResponseDto.from(item, this));
    }

    @Transactional(readOnly = true)
    public ReadItemDetailResponseDto findItemById(Long itemId) {

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);

        return ReadItemDetailResponseDto.from(item);
    }

    @Transactional
    public void deleteItem(TokenClaim tokenClaim, Long itemId) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        item.validateUserIsOwner(user);
        item.deleteItem();

        Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
        locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);
    }

    @Transactional
    public ItemResponseDto updateItem(TokenClaim tokenClaim, Long itemId, UpdateItemRequestDto request) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        item.validateUserIsOwner(user);
        item.updateItem(request);

        return ItemResponseDto.from(item);
    }
    
    /**
     * 이미지 URL 리스트를 JSON으로 안전하게 직렬화합니다.
     * 쉼표 포함 URL도 안전하게 처리할 수 있습니다.
     */
    private String serializeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 직렬화 실패", e);
            throw new RuntimeException("이미지 URL 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * JSON 문자열을 이미지 URL 리스트로 역직렬화합니다.
     */
    public List<String> deserializeImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 역직렬화 실패: {}", imageUrlsJson, e);
            // 레거시 데이터 호환을 위해 쉼표 구분 처리도 지원
            return List.of(imageUrlsJson.split(","));
        }
    }
}
