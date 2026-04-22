package com.sparta.spartatigers.domain.exchangerequest.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
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

        // 생성 즉시 DirectRoom 맵핑을 생성합니다 (초기에는 PENDING 상태이므로 프론트에서 UI 제어)
        directRoomService.createRoom(saved.getId(), sender.getId());

        return saved.getId();
    }

    public Page<ReceiveRequestResponseDto> findAllReceiveRequest(TokenClaim tokenClaim, Pageable pageable) {

        User user = getUser(tokenClaim.getUserId());
        Page<ExchangeRequest> exchangeRequestList = exchangeRequestRepository.findAllReceiveRequest(
            user.getId(), pageable);

        return mapToResponseWithRoomId(exchangeRequestList);
    }

    @Transactional
    public ExchangeRoomResponseDto updateRequestStatus(Long exchangeRequestId, UpdateExchangeRequestDto request,
        TokenClaim tokenClaim) {

        User user = getUser(tokenClaim.getUserId());
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findExchangeRequestByIdOrElseThrow(
            exchangeRequestId);
        exchangeRequest.validateReceiverIsOwner(user);
        exchangeRequest.updateStatus(request.status());

        if (exchangeRequest.getStatus() == ExchangeStatus.ACCEPTED) {
            rejectOtherPendingRequests(exchangeRequest.getItem(), exchangeRequest.getId());
        }

        if (exchangeRequest.getStatus() == ExchangeStatus.ACCEPTED) {
            DirectRoom room = directRoomRepository.findByExchangeRequestId(exchangeRequestId)
                    .orElseThrow(() -> new InvalidRequestException(ExceptionCode.DIRECT_ROOM_NOT_FOUND));
            sendNotificationToSender(exchangeRequest, "교환 요청 수락", "교환 요청이 수락되었습니다. 채팅방에서 대화를 시작해보세요!");
            return ExchangeRoomResponseDto.from(DirectRoomCreateResponseDto.from(room));
        }

        if (exchangeRequest.getStatus() == ExchangeStatus.REJECTED) {
            sendNotificationToSender(exchangeRequest, "교환 요청 거절", "아쉽게도 교환 요청이 거절되었습니다.");
            // 거절 내역(History) 유지를 위해 삭제하지 않음. DirectRoom 또한 보존하지만 프론트엔드에서 disabled 처리됨.
        }

        return null;
    }

    private void rejectOtherPendingRequests(Item item, Long acceptedRequestId) {
        java.util.List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByItemIdAndStatus(item.getId(), ExchangeStatus.PENDING);
        for (ExchangeRequest req : pendingRequests) {
            if (!req.getId().equals(acceptedRequestId)) {
                req.updateStatus(ExchangeStatus.REJECTED);
            }
        }
    }

    private void sendNotificationToSender(ExchangeRequest exchangeRequest, String title, String body) {
        User sender = exchangeRequest.getSender();
        if (sender != null && sender.getDeviceToken() != null && !sender.getDeviceToken().isBlank()) {
            try {
                fcmService.sendMessageToToken(sender.getDeviceToken(), title, body);
            } catch (Exception e) {
                // 알림 발송은 핵심 비즈니스 로직(상태 업데이트)을 중단시키지 않아야 합니다.
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

        return mapToResponseWithRoomId(requests);
    }

    private Page<ReceiveRequestResponseDto> mapToResponseWithRoomId(Page<ExchangeRequest> requests) {
        List<Long> requestIds = requests.stream()
            .map(ExchangeRequest::getId)
            .collect(Collectors.toList());

        Map<Long, Long> roomIdMap = new java.util.HashMap<>();
        if (!requestIds.isEmpty()) {
            List<DirectRoom> rooms = directRoomRepository.findByExchangeRequestIdIn(requestIds);
            log.info("[mapToResponseWithRoomId] 조회된 요청 ID 개수: {}, 조회된 채팅방 개수: {}", requestIds.size(), rooms.size());
            
            for (DirectRoom room : rooms) {
                if (room.getExchangeRequest() != null) {
                    roomIdMap.put(room.getExchangeRequest().getId(), room.getId());
                }
            }
        }

        return requests.map(req -> {
            Long roomId = roomIdMap.get(req.getId());
            if (roomId == null) {
                log.warn("[mapToResponseWithRoomId] 요청 ID {}에 매핑된 채팅방을 찾을 수 없습니다.", req.getId());
            }
            return ReceiveRequestResponseDto.from(req, roomId);
        });
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
