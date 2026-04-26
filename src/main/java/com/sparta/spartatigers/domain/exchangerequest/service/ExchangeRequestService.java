package com.sparta.spartatigers.domain.exchangerequest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sparta.spartatigers.domain.exchangerequest.dto.response.SendRequestResponseDto;
import com.sparta.spartatigers.global.exception.external.FirebaseException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomCreateResponseDto;
import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.directRoom.service.DirectRoomService;
import com.sparta.spartatigers.domain.exchangerequest.dto.request.ExchangeRequestDto;
import com.sparta.spartatigers.domain.exchangerequest.dto.request.UpdateExchangeRequestDto;
import com.sparta.spartatigers.domain.exchangerequest.dto.response.ExchangeRoomResponseDto;
import com.sparta.spartatigers.domain.exchangerequest.dto.response.ReceiveRequestResponseDto;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeStatus;
import com.sparta.spartatigers.domain.exchangerequest.repository.ExchangeRequestRepository;
import com.sparta.spartatigers.domain.item.event.ItemLocationUpdatedEvent;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import com.sparta.spartatigers.global.firebase.FCMService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final DirectRoomService directRoomService;
    private final DirectRoomRepository directRoomRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FCMService fcmService;

    @Transactional
    public Long createExchangeRequest(ExchangeRequestDto request, TokenClaim tokenClaim) {

        User sender = getUser(tokenClaim.getUserId());
        User receiver = getUser(request.receiverId());
        Item item = itemRepository.findByIdWithLockOrElseThrow(request.itemId());

        item.validateSenderIsNotOwner(sender);
        item.validateReceiverIsOwner(receiver);

        checkDuplicateExchangeRequest(sender.getId(), receiver.getId(), item.getId());

        String have = request.have();

        ExchangeRequest exchangeRequest = ExchangeRequest.of(item, sender, receiver, have);
        ExchangeRequest saved = exchangeRequestRepository.save(exchangeRequest);

        // [FIX] 교환 요청 생성 시점에는 채팅방을 만들지 않고 PENDING 상태 유지
        // 채팅방은 교환 요청이 ACCEPTED 될 때 생성됨

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Page<ReceiveRequestResponseDto> findAllReceiveRequest(TokenClaim tokenClaim, Pageable pageable) {
        User user = getUser(tokenClaim.getUserId());
        Page<ExchangeRequest> exchangeRequestList = exchangeRequestRepository.findAllReceiveRequest(
            user.getId(), pageable);

        return mapToReceiveResponse(exchangeRequestList);
    }

    @Transactional(readOnly = true)
    public Page<SendRequestResponseDto> findAllSendRequest(TokenClaim tokenClaim, Pageable pageable) {
        User user = getUser(tokenClaim.getUserId());
        Page<ExchangeRequest> exchangeRequestList = exchangeRequestRepository.findAllSentRequest(
            user.getId(), pageable);

        return mapToSendResponse(exchangeRequestList);
    }

    @Transactional
    public ExchangeRoomResponseDto updateRequestStatus(Long exchangeRequestId, UpdateExchangeRequestDto request,
        TokenClaim tokenClaim) {

        User user = getUser(tokenClaim.getUserId());
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findExchangeRequestByIdOrElseThrow(
            exchangeRequestId);
        exchangeRequest.validateReceiverIsOwner(user);
        exchangeRequest.updateStatus(request.status());

        // [FIX] 중복 ACCEPTED 분기 통합
        if (exchangeRequest.getStatus() == ExchangeStatus.ACCEPTED) {
            // 다른 PENDING 요청 자동 거절 (내부에서 거절 알림도 함께 발송)
            rejectOtherPendingRequests(exchangeRequest.getItem(), exchangeRequest.getId());
            // [FIX] 수락 시점에 명시적으로 채팅방 생성
            DirectRoomCreateResponseDto roomCreateDto = directRoomService.createRoom(exchangeRequestId, user.getId());
            
            // [FIX] 문제 3: 트랜잭션 롤백 시 알림만 남는 문제 방지 — 커밋 보장 후 알림 발송
            // [FIX] Detached 상태에서의 Lazy 로딩 방지를 위해 필요한 값 미리 추출
            final String deviceToken = exchangeRequest.getSender().getDeviceToken();
            final Long senderId = exchangeRequest.getSender().getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNotificationToSenderDirectly(deviceToken, senderId, "교환 요청 수락", "교환 요청이 수락되었습니다. 채팅방에서 대화를 시작해보세요!");
                }
            });
            return ExchangeRoomResponseDto.from(roomCreateDto);
        }

        if (exchangeRequest.getStatus() == ExchangeStatus.REJECTED) {
            // [FIX] 문제 3: 트랜잭션 롤백 시 알림만 남는 문제 방지 — 커밋 보장 후 알림 발송
            // [FIX] Detached 상태에서의 Lazy 로딩 방지를 위해 필요한 값 미리 추출
            final String deviceToken = exchangeRequest.getSender().getDeviceToken();
            final Long senderId = exchangeRequest.getSender().getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNotificationToSenderDirectly(deviceToken, senderId, "교환 요청 거절", "아쉽게도 교환 요청이 거절되었습니다.");
                }
            });
            // 거절 내역(History) 유지를 위해 삭제하지 않음. DirectRoom 또한 보존하지만 프론트엔드에서 disabled 처리됨.
            return ExchangeRoomResponseDto.rejected(exchangeRequestId);
        }

        // 기본적으로 null 대신 rejected 또는 적절한 DTO 반환하여 호출부 NPE 방어
        return ExchangeRoomResponseDto.rejected(exchangeRequestId);
    }

    private void rejectOtherPendingRequests(Item item, Long acceptedRequestId) {
        // [FIX] 동시 수락/요청 시 정합성 보장을 위해 비관적 락(PESSIMISTIC_WRITE) 적용
        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByItemIdAndStatusForUpdate(item.getId(), ExchangeStatus.PENDING);
        for (ExchangeRequest req : pendingRequests) {
            if (!req.getId().equals(acceptedRequestId)) {
                req.updateStatus(ExchangeStatus.REJECTED);
                // [FIX] 자동 거절된 요청자에게도 알림 발송
                final String deviceToken = req.getSender().getDeviceToken();
                final Long senderId = req.getSender().getId();
                
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendNotificationToSenderDirectly(deviceToken, senderId, "교환 요청 거절", "아쉽게도 교환 요청이 거절되었습니다.");
                    }
                });
            }
        }
    }

    private void sendNotificationToSenderDirectly(String deviceToken, Long senderId, String title, String body) {
        if (deviceToken != null && !deviceToken.isBlank()) {
            try {
                fcmService.sendMessageToToken(deviceToken, title, body);
            } catch (FirebaseException e) {
                log.warn("[FCM] 알림 발송 실패 - userId: {}, title: {}, error: [{}] {}",
                    senderId, title, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Transactional
    public void completeExchange(Long exchangeRequestId, TokenClaim tokenClaim) {

        User user = getUser(tokenClaim.getUserId());
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findAcceptedRequestByIdOrElseThrow(
            exchangeRequestId);
        exchangeRequest.validateReceiverIsOwner(user);

        Item item = itemRepository.findById(exchangeRequest.getItem().getId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND));
        item.complete();

        exchangeRequest.complete();

        DirectRoom room = directRoomRepository.findByExchangeRequestId(exchangeRequestId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.DIRECT_ROOM_NOT_FOUND));
        room.complete();

        ItemLocationUpdatedEvent event = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM", 
            Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
        applicationEventPublisher.publishEvent(event);
    }

    @Transactional(readOnly = true)
    public Page<ReceiveRequestResponseDto> findMyExchangeRequests(String role, ExchangeStatus status, Pageable pageable, TokenClaim tokenClaim) {
        Long userId = tokenClaim.getUserId();
        Page<ExchangeRequest> requests;

        if ("sender".equals(role)) {
            if (status == null) {
                requests = exchangeRequestRepository.findAllSentRequest(userId, pageable);
            } else {
                requests = exchangeRequestRepository.findAllSentRequestWithStatus(userId, status, pageable);
            }
        } else if ("receiver".equals(role)) {
            if (status == null) {
                requests = exchangeRequestRepository.findAllReceiveRequest(userId, pageable);
            } else {
                requests = exchangeRequestRepository.findAllReceiveRequestWithStatus(userId, status, pageable);
            }
        } else {
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }

        return mapToReceiveResponse(requests);
    }

    private Page<ReceiveRequestResponseDto> mapToReceiveResponse(Page<ExchangeRequest> requests) {
        Map<Long, Long> roomIdMap = getRoomIdMap(requests);
        return requests.map(req -> ReceiveRequestResponseDto.from(req, roomIdMap.get(req.getId())));
    }

    private Page<SendRequestResponseDto> mapToSendResponse(Page<ExchangeRequest> requests) {
        Map<Long, Long> roomIdMap = getRoomIdMap(requests);
        return requests.map(req -> SendRequestResponseDto.from(req, roomIdMap.get(req.getId())));
    }

    private Map<Long, Long> getRoomIdMap(Page<ExchangeRequest> requests) {
        List<Long> requestIds = requests.stream()
            .map(ExchangeRequest::getId)
            .collect(Collectors.toList());

        // [FIX] 문제 5: FQCN new java.util.HashMap<>() → import로 정리
        Map<Long, Long> roomIdMap = new HashMap<>();
        if (!requestIds.isEmpty()) {
            List<DirectRoom> rooms = directRoomRepository.findByExchangeRequestIdIn(requestIds);
            for (DirectRoom room : rooms) {
                if (room.getExchangeRequest() != null) {
                    roomIdMap.put(room.getExchangeRequest().getId(), room.getId());
                }
            }
        }
        return roomIdMap;
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
    }

    private void checkDuplicateExchangeRequest(Long senderId, Long receiverId, Long itemId) {

        boolean isExisted =
            exchangeRequestRepository.existsBySenderIdAndReceiverIdAndItemId(senderId, receiverId, itemId);

        if (isExisted) {
            throw new InvalidRequestException(ExceptionCode.EXCHANGE_REQUEST_DUPLICATED);
        }
    }
}
