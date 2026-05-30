package com.sparta.spartatigers.domain.core.trade.service;

import com.sparta.spartatigers.domain.core.trade.dto.request.FindItemByIdRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateItemStatusRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.foundation.common.event.ItemLocationUpdatedEvent;
import com.sparta.spartatigers.domain.core.trade.model.Item;
import com.sparta.spartatigers.domain.core.trade.model.ItemStatus;
import com.sparta.spartatigers.domain.core.trade.repository.ItemRepository;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.spartatigers.domain.support.chat.service.LocationService;
import com.sparta.spartatigers.domain.core.trade.dto.request.ItemCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.foundation.common.event.ItemStatusChangedEvent;
import com.sparta.spartatigers.domain.core.trade.repository.ExchangeRequestRepository;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeRequest;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;
import com.sparta.spartatigers.global.firebase.service.FCMService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private static final double SEARCH_RADIUS_KM = 0.05;
    // [FIX] 문제 4: 기본 반경 매직 넘버 2.0 상수 추출 — 운영 중 정책 변경 시 일관 수정 용이
    private static final double DEFAULT_LOCATION_SEARCH_RADIUS_KM = 2.0;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocationService locationService;
    private final ObjectMapper objectMapper;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final FCMService fcmService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public void validateCanCreateItem(TokenClaim tokenClaim) {
        checkDuplicateItemByUserId(tokenClaim.getUserId());
    }

    private void checkDuplicateItemByUserId(Long userId) {
        if (itemRepository.existsByUserIdAndStatus(userId, ItemStatus.REGISTERED)) {
            throw new InvalidRequestException(ExceptionCode.ITEM_ALREADY_EXISTS);
        }
    }

    @Transactional
    public ItemResponseDto createItemWithImages(ItemCreateRequest request, TokenClaim tokenClaim,
            List<String> imageUrls) {
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
                request.title(), request.description(), latitude, longitude, address, request.desiredItem(),
                ItemStatus.REGISTERED, user, LocalDate.now(clock));

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

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId, LocalDate.now(clock));
        item.validateUserIsOwner(user);

        switch (request.action()) {
            case COMPLETE -> {
                item.complete();
                // 상태별 개별 조회를 없애고 IN 쿼리로 한 번에 메모리에 적재하여 N+1 이슈 완화
                List<ExchangeRequest> activeRequests = exchangeRequestRepository.findByItemIdAndStatusIn(
                        item.getId(), List.of(ExchangeStatus.ACCEPTED, ExchangeStatus.PENDING));

                for (ExchangeRequest req : activeRequests) {
                    if (req.getStatus() == ExchangeStatus.ACCEPTED) {
                        req.updateStatus(ExchangeStatus.COMPLETED);
                        applicationEventPublisher.publishEvent(new ItemStatusChangedEvent(req.getId(), "거래가 완료되었습니다."));
                    } else if (req.getStatus() == ExchangeStatus.PENDING) {
                        req.updateStatus(ExchangeStatus.REJECTED);
                    }
                }

                ItemLocationUpdatedEvent completeEvent = new ItemLocationUpdatedEvent(item.getUser().getId(),
                        "REMOVE_ITEM",
                        Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
                applicationEventPublisher.publishEvent(completeEvent);
            }

            case CANCEL -> {
                item.reopen(LocalDate.now(clock));
                List<ExchangeRequest> activeRequests = exchangeRequestRepository.findByItemIdAndStatusIn(
                        item.getId(), List.of(ExchangeStatus.ACCEPTED, ExchangeStatus.PENDING));

                for (ExchangeRequest req : activeRequests) {
                    ExchangeStatus previousStatus = req.getStatus();
                    req.updateStatus(ExchangeStatus.REJECTED);

                    // [FIX] 상태 변경 전 ACCEPTED 였던 경우에만 시스템 메시지 발행
                    if (previousStatus == ExchangeStatus.ACCEPTED) {
                        applicationEventPublisher
                                .publishEvent(new ItemStatusChangedEvent(req.getId(), "STATUS_UPDATED"));
                    }
                }

                ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item, this);
                ItemLocationUpdatedEvent cancelEvent = new ItemLocationUpdatedEvent(item.getUser().getId(), "ADD_ITEM",
                        newItemDto);
                applicationEventPublisher.publishEvent(cancelEvent);
            }
            case DELETE -> {
                item.deleteItem();
                ItemLocationUpdatedEvent deleteEvent = new ItemLocationUpdatedEvent(item.getUser().getId(),
                        "REMOVE_ITEM",
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
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR)).getId();

        // 좌표 기반 검색 여부 확인
        if (latitude != null && longitude != null) {
            // [FIX] 문제 4: 2.0 값을 DEFAULT_LOCATION_SEARCH_RADIUS_KM 상수로 대체
            double searchRadius = radius != null ? radius : DEFAULT_LOCATION_SEARCH_RADIUS_KM;

            // [FIX] 공간 쿼리 최적화: Bounding Box 계산 (1도 위도 ~= 111km)
            double latDiff = searchRadius / 111.0;
            double lonDiff = searchRadius / (111.0 * Math.cos(Math.toRadians(latitude)));

            double minLat = latitude - latDiff;
            double maxLat = latitude + latDiff;
            double minLon = longitude - lonDiff;
            double maxLon = longitude + lonDiff;

            return itemRepository.findAllItemsByLocation(
                    ItemStatus.REGISTERED,
                    LocalDate.now(clock),
                    userId,
                    latitude,
                    longitude,
                    minLat,
                    maxLat,
                    minLon,
                    maxLon,
                    searchRadius,
                    pageable).map(item -> ReadItemResponseDto.from(item, this));
        } else {
            // 기존 로직: 근처 사용자 기반 검색
            // [FIX] 문제 3: locationService.findUsersNearBy()가 불변 List(List.of 등)를 반환하는 경우
            // add() 호출 시 UnsupportedOperationException 발생
            // 방어적으로 ArrayList로 복사하여 수정 가능한 리스트로 보장
            List<Long> nearByUserIds = new ArrayList<>(locationService.findUsersNearBy(userId, SEARCH_RADIUS_KM));
            nearByUserIds.add(userId);

            return itemRepository.findAllItems(ItemStatus.REGISTERED, LocalDate.now(clock), nearByUserIds, pageable)
                    .map(item -> ReadItemResponseDto.from(item, this));
        }
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findMyItems(TokenClaim tokenClaim, Pageable pageable) {
        Long userId = userRepository.findById(tokenClaim.getUserId())
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR))
                .getId();

        return itemRepository.findAllMyItems(ItemStatus.REGISTERED, LocalDate.now(clock), userId, pageable)
                .map(item -> ReadItemResponseDto.from(item, this));
    }

    @Transactional(readOnly = true)
    public ReadItemDetailResponseDto findItemById(Long itemId, FindItemByIdRequestDto request) {

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId, LocalDate.now(clock));
        Double lat = request.latitude() != null ? request.latitude().doubleValue() : null;
        Double lon = request.longitude() != null ? request.longitude().doubleValue() : null;
        Integer distance = locationService.calculateDistance(lat, lon, item.getLatitude(), item.getLongitude());

        return ReadItemDetailResponseDto.from(item, distance);
    }

    @Transactional
    public void deleteItem(TokenClaim tokenClaim, Long itemId) {

        User user = userRepository.findById(tokenClaim.getUserId())
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId, LocalDate.now(clock));
        item.validateUserIsOwner(user);
        item.deleteItem();

        ItemLocationUpdatedEvent event = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM",
                Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
        applicationEventPublisher.publishEvent(event);
    }

    @Transactional
    public ItemResponseDto updateItem(TokenClaim tokenClaim, Long itemId, UpdateItemRequestDto request) {

        User user = userRepository.findById(tokenClaim.getUserId())
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId, LocalDate.now(clock));
        item.validateUserIsOwner(user);
        item.updateItem(request);

        return ItemResponseDto.from(item);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveItem(TokenClaim tokenClaim) {
        return itemRepository.existsByUserIdAndStatus(tokenClaim.getUserId(), ItemStatus.REGISTERED);
    }

    // 이미지 URL 리스트를 JSON 문자열로 직렬화
    public String serializeImageUrls(List<String> imageUrls) {
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 직렬화 실패: {}", e.getMessage(), e);
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    // JSON 문자열을 이미지 URL 리스트로 역직렬화
    public List<String> deserializeImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 역직렬화 실패: ", e);
            return Collections.emptyList();
        }
    }

}
