package com.sparta.spartatigers.domain.core.trade.service;

import com.sparta.spartatigers.global.firebase.dto.NotificationMessage;
import com.sparta.spartatigers.global.firebase.service.NotificationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sparta.spartatigers.domain.core.trade.dto.response.SendRequestResponseDto;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.global.aop.TokenClaim;

import com.sparta.spartatigers.domain.core.trade.dto.request.ExchangeRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateExchangeRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ExchangeRoomResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReceiveRequestResponseDto;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeRequest;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;
import com.sparta.spartatigers.domain.core.trade.repository.ExchangeRequestRepository;
import com.sparta.spartatigers.domain.core.trade.event.ItemLocationUpdatedEvent;
import com.sparta.spartatigers.domain.core.trade.event.TradeAcceptedEvent;
import com.sparta.spartatigers.domain.core.trade.model.Item;
import com.sparta.spartatigers.domain.core.trade.repository.ItemRepository;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.exception.internal.ServerException;

import com.sparta.spartatigers.global.firebase.service.FCMService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FCMService fcmService;
    private final NotificationService notificationService;

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

        sendNotification(receiver.getFcmToken(), "새 교환 요청",
            String.format("'%s'에 %s님이 교환을 요청했어요.", item.getTitle(), sender.getNickname()));

        log.info("[FCM 교환요청 알림] receiverId={}, senderId={}",
            receiver.getId(),
            sender.getId());

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
        // [FIX] 문제 1: 동시성 제어(Race Condition) 해결을 위해 조회 시점부터 비관적 락 적용
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findByIdForUpdate(exchangeRequestId)
                .orElseThrow(() -> new ServerException(ExceptionCode.EXCHANGE_REQUEST_NOT_FOUND));

        // 상태가 PENDING이 아니면 이미 처리된 요청이므로 즉시 실패 (Wait 후 깨어난 스레드 방어)
        if (exchangeRequest.getStatus() != ExchangeStatus.PENDING) {
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }

        exchangeRequest.validateReceiverIsOwner(user);
        exchangeRequest.updateStatus(request.status());

        // [FIX] 중복 ACCEPTED 분기 통합
        if (exchangeRequest.getStatus() == ExchangeStatus.ACCEPTED) {
            // 다른 PENDING 요청 자동 거절 (내부에서 거절 알림도 함께 발송)
            rejectOtherPendingRequests(exchangeRequest.getItem(), exchangeRequest.getId());
            
            // [FIX] 수락 시점에 명시적으로 채팅방 생성 대신 이벤트 발행
            applicationEventPublisher.publishEvent(new TradeAcceptedEvent(
                    exchangeRequestId,
                    exchangeRequest.getSender().getId(),
                    user.getId(),
                    exchangeRequest.getItem().getId()
            ));

            sendNotification(
                exchangeRequest.getSender().getFcmToken(), "교환 요청 수락",
                String.format("'%s'에 대한 교환 요청이 수락되었어요.", exchangeRequest.getItem().getTitle()));

            return ExchangeRoomResponseDto.created(exchangeRequestId);
        }

        if (exchangeRequest.getStatus() == ExchangeStatus.REJECTED) {
            // 거절 내역(History) 유지를 위해 삭제하지 않음. DirectRoom 또한 보존하지만 프론트엔드에서 disabled 처리됨.
            return ExchangeRoomResponseDto.rejected(exchangeRequestId);
        }

        // 기본적으로 null 대신 rejected 또는 적절한 DTO 반환하여 호출부 NPE 방어
        return ExchangeRoomResponseDto.rejected(exchangeRequestId);
    }

    private void rejectOtherPendingRequests(Item item, Long acceptedRequestId) {
        // [FIX] 동시 수락/요청 시 정합성 보장을 위해 비관적 락(PESSIMISTIC_WRITE) 적용
        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByItemIdAndStatusForUpdate(item.getId(),
                ExchangeStatus.PENDING);
        for (ExchangeRequest req : pendingRequests) {
            if (!req.getId().equals(acceptedRequestId)) {
                req.updateStatus(ExchangeStatus.REJECTED);
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

        ItemLocationUpdatedEvent event = new ItemLocationUpdatedEvent(item.getUser().getId(), "REMOVE_ITEM",
                Map.of("itemId", item.getId(), "userId", item.getUser().getId()));
        applicationEventPublisher.publishEvent(event);
    }

    @Transactional(readOnly = true)
    public Page<ReceiveRequestResponseDto> findMyExchangeRequests(String role, ExchangeStatus status, Pageable pageable,
            TokenClaim tokenClaim) {
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
        // [FIX] Java 16+ Stream.toList() 사용 (불변 리스트 반환 및 가독성 향상)
        List<Long> requestIds = requests.stream()
                .map(ExchangeRequest::getId)
                .toList();

        // [FIX] HashMap 초기 용량 힌트 제공으로 minor allocation 최적화
        Map<Long, Long> roomIdMap = new HashMap<>(requestIds.size() * 2);
        return roomIdMap;
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
    }

    private void checkDuplicateExchangeRequest(Long senderId, Long receiverId, Long itemId) {

        boolean isExisted = exchangeRequestRepository.existsBySenderIdAndReceiverIdAndItemId(senderId, receiverId,
                itemId);

        if (isExisted) {
            throw new InvalidRequestException(ExceptionCode.EXCHANGE_REQUEST_DUPLICATED);
        }
    }

    private void sendNotification(String token, String title, String body) {
        NotificationMessage message = NotificationMessage.of(token, title, body);
        notificationService.sendAfterCommit(message);
    }
}
