package com.sparta.spartatigers.domain.item.service;

import com.sparta.spartatigers.domain.item.dto.request.FindItemByIdRequestDto;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.sparta.spartatigers.domain.stompchat.service.LocationService;
import com.sparta.spartatigers.domain.item.dto.request.ItemCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.exchangerequest.repository.ExchangeRequestRepository;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeStatus;
import com.sparta.spartatigers.global.firebase.FCMService;
import com.sparta.spartatigers.global.exception.external.FirebaseException;

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
    private final DirectRoomRepository directRoomRepository;
    private final FCMService fcmService;
    private final com.sparta.spartatigers.domain.stompchat.pubsub.RedisDirectMessagePublisher redisDirectMessagePublisher;

    @Transactional(readOnly = true)
    public void validateCanCreateItem(TokenClaim tokenClaim) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));
            
        if (itemRepository.existsByUserIdAndStatus(user.getId(), ItemStatus.REGISTERED)) {
            throw new InvalidRequestException(ExceptionCode.ITEM_ALREADY_EXISTS);
        }
    }

    @Transactional
    public ItemResponseDto createItemWithImages(ItemCreateRequest request, TokenClaim tokenClaim, List<String> imageUrls) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        // 이미 등록된 활성(REGISTERED) 상태의 아이템이 있는지 확인
        if (itemRepository.existsByUserIdAndStatus(user.getId(), ItemStatus.REGISTERED)) {
            throw new InvalidRequestException(ExceptionCode.ITEM_ALREADY_EXISTS);
        }

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
                // [FIX] 문제 2: 상태별 개별 조회를 없애고 IN 쿼리로 한 번에 메모리에 적재하여 N+1 이슈 완화
                List<ExchangeRequest> activeRequests = exchangeRequestRepository.findByItemIdAndStatusIn(
                    item.getId(), List.of(ExchangeStatus.ACCEPTED, ExchangeStatus.PENDING));

                for (ExchangeRequest req : activeRequests) {
                    if (req.getStatus() == ExchangeStatus.ACCEPTED) {
                        req.updateStatus(ExchangeStatus.COMPLETED);
                        directRoomRepository.findByExchangeRequestId(req.getId())
                            .ifPresent(room -> {
                                room.complete();
                                publishSystemStatusUpdatedMessage(room.getId());
                            });
                    } else if (req.getStatus() == ExchangeStatus.PENDING) {
                        req.updateStatus(ExchangeStatus.REJECTED);
                        // [FIX] 자동 거절되는 PENDING 요청자들에게도 알림 발송 (ExchangeRequestService와 정책 통일)
                        sendNotificationSafely(req, "교환 요청 거절", "다른 사용자와 교환이 완료되어 요청이 거절되었습니다.");
                    }
                }

                ItemLocationUpdatedEvent completeEvent = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM",
                        Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
                applicationEventPublisher.publishEvent(completeEvent);
            }

            case CANCEL -> {
                item.reopen();
                List<ExchangeRequest> activeRequests = exchangeRequestRepository.findByItemIdAndStatusIn(
                    item.getId(), List.of(ExchangeStatus.ACCEPTED, ExchangeStatus.PENDING));

                for (ExchangeRequest req : activeRequests) {
                    ExchangeStatus previousStatus = req.getStatus();
                    req.updateStatus(ExchangeStatus.REJECTED);
                    // PENDING 및 ACCEPTED 상태였던 사용자들에게 교환 취소(재오픈) 알림 발송
                    sendNotificationSafely(req, "교환 취소", "상대방의 사정으로 교환이 취소되었습니다.");
                    
                    // [FIX] 상태 변경 전 ACCEPTED 였던 경우에만 시스템 메시지 발행
                    if (previousStatus == ExchangeStatus.ACCEPTED) {
                        directRoomRepository.findByExchangeRequestId(req.getId())
                            .ifPresent(room -> publishSystemStatusUpdatedMessage(room.getId()));
                    }
                }

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
            // [FIX] 문제 4: 2.0 값을 DEFAULT_LOCATION_SEARCH_RADIUS_KM 상수로 대체
            double searchRadius = radius != null ? radius : DEFAULT_LOCATION_SEARCH_RADIUS_KM;
            return itemRepository.findAllItemsByLocation(
                ItemStatus.REGISTERED,
                LocalDate.now(),
                userId,
                latitude,
                longitude,
                searchRadius,
                pageable
            ).map(item -> ReadItemResponseDto.from(item, this));
        } else {
            // 기존 로직: 근처 사용자 기반 검색
            // [FIX] 문제 3: locationService.findUsersNearBy()가 불변 List(List.of 등)를 반환하는 경우 add() 호출 시 UnsupportedOperationException 발생
            // 방어적으로 ArrayList로 복사하여 수정 가능한 리스트로 보장
            List<Long> nearByUserIds = new ArrayList<>(locationService.findUsersNearBy(userId, SEARCH_RADIUS_KM));
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
    public ReadItemDetailResponseDto findItemById(Long itemId, FindItemByIdRequestDto request) {

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        Integer distance = locationService.calculateDistance(request.latitude(),
            request.longitude(), item);

        return ReadItemDetailResponseDto.from(item, distance);
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
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("이미지 URL 역직렬화 실패: ", e);
            return Collections.emptyList();
        }
    }

    private void sendNotificationSafely(ExchangeRequest exchangeRequest, String title, String body) {
        User sender = exchangeRequest.getSender();
        if (sender == null || sender.getDeviceToken() == null || sender.getDeviceToken().isBlank()) {
            return;
        }

        // [FIX] 트랜잭션 종료(Detached) 후 Lazy 로딩 방지를 위해 필요한 값 미리 캡처
        final String deviceToken = sender.getDeviceToken();
        final Long senderId = sender.getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    fcmService.sendMessageToToken(deviceToken, title, body);
                } catch (FirebaseException e) {
                    log.warn("[FCM 발송 실패] 수신자: {}, 사유: {}", senderId, e.getMessage());
                }
            }
        });
    }

    private void publishSystemStatusUpdatedMessage(Long roomId) {
        Map<String, Object> payload = Map.of(
            "type", "SYSTEM",
            "action", "STATUS_UPDATED",
            "roomId", roomId
        );
        redisDirectMessagePublisher.publish("/server/directRoom/" + roomId, payload);
    }
}
