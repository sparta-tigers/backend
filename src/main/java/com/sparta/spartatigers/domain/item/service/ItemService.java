package com.sparta.spartatigers.domain.item.service;

import com.sparta.spartatigers.domain.item.dto.request.UpdateItemStatusRequestDto;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.event.ItemLocationUpdatedEvent;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.spartatigers.domain.stompchat.service.LocationService;
import com.sparta.spartatigers.domain.item.dto.request.ItemCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final double SEARCH_RADIUS_KM = 0.05;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocationService locationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ItemResponseDto createItemWithImages(ItemCreateRequest request, TokenClaim tokenClaim, List<String> imageUrls) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        // 이미지 URL 리스트를 JSON으로 안전하게 직렬화
        String imageUrlsJson = serializeImageUrls(imageUrls);

        Double latitude = null;
        Double longitude = null;
        String address = null;

        ItemCreateRequest.Location location = request.location();
        if (location != null) {
            if (location.latitude() == null || location.longitude() == null) {
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }
            latitude = location.latitude();
            longitude = location.longitude();
            address = location.address();
        }

        Item item = new Item(request.category(), imageUrlsJson, request.seatInfo(), 
                request.title(), request.description(), latitude, longitude, address, request.desiredItem(), ItemStatus.REGISTERED, user, LocalDate.now());

        Item savedItem = itemRepository.save(item);

        ReadItemResponseDto newItemDto = ReadItemResponseDto.from(savedItem, this);
        ItemLocationUpdatedEvent event = new ItemLocationUpdatedEvent(user.getId(), "ADD_ITEM", newItemDto);
        applicationEventPublisher.publishEvent(event);

        return ItemResponseDto.from(savedItem);
    }

    @Transactional
    public void updateItemStatus(TokenClaim tokenClaim, Long itemId, UpdateItemStatusRequestDto request) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        item.validateUserIsOwner(user);

        switch (request.action()) {
            case COMPLETE -> {
                item.complete();
                ItemLocationUpdatedEvent completeEvent = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM",
                        Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
                applicationEventPublisher.publishEvent(completeEvent);
            }

            case CANCEL -> {
                item.reopen();
                ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item, this);
                ItemLocationUpdatedEvent cancelEvent = new ItemLocationUpdatedEvent(item.getUser().getId(), "ADD_ITEM", newItemDto);
                applicationEventPublisher.publishEvent(cancelEvent);
            }
            case DELETE -> {
                item.deleteItem();
                ItemLocationUpdatedEvent deleteEvent = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM",
                        Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
                applicationEventPublisher.publishEvent(deleteEvent);
            }
            default -> throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findAllItems(TokenClaim tokenClaim, Pageable pageable, 
            Double latitude, Double longitude, Double radius) {

        Long userId = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR)).getId();

        // 좌표 기반 검색 여부 확인
        if (latitude != null && longitude != null) {
            double searchRadius = radius != null ? radius : 2.0; // 기본 반경 2km
            return itemRepository.findAllItemsByLocation(
                ItemStatus.REGISTERED, 
                LocalDate.now(), 
                latitude, 
                longitude, 
                searchRadius, 
                pageable
            ).map(item -> ReadItemResponseDto.from(item, this));
        } else {
            // 기존 로직: 근처 사용자 기반 검색
            List<Long> nearByUserIds = locationService.findUsersNearBy(userId, SEARCH_RADIUS_KM);
            nearByUserIds.add(userId);

            return itemRepository.findAllItems(ItemStatus.REGISTERED, LocalDate.now(), nearByUserIds, pageable)
                    .map(item -> ReadItemResponseDto.from(item, this));
        }
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findMyItems(TokenClaim tokenClaim, Pageable pageable) {
        Long userId = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR))
            .getId();

        return itemRepository.findAllMyItems(ItemStatus.REGISTERED, LocalDate.now(), userId, pageable)
                .map(item -> ReadItemResponseDto.from(item, this));
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

        ItemLocationUpdatedEvent event = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM", 
            Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
        applicationEventPublisher.publishEvent(event);
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

    private String serializeImageUrls(List<String> imageUrls) {
        // 1. null 또는 빈 리스트 사전 방어
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 직렬화 실패: {}", e.getMessage());
            // 2 & 3. 조용한 실패를 막고 예외를 던져 트랜잭션 롤백 유도
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    public List<String> deserializeImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 역직렬화 실패: {}", imageUrlsJson, e);
            // 레거시 데이터 호환을 위해 쉼표 구분 처리도 지원
            if (imageUrlsJson.startsWith("[") && imageUrlsJson.endsWith("]")) {
                String content = imageUrlsJson.substring(1, imageUrlsJson.length() - 1);
                if (content.trim().isEmpty()) {
                    return List.of();
                }
                return java.util.Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            }
            return List.of();
        }
    }
}
