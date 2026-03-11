package com.sparta.spartatigers.domain.exchangerequest.service;

import java.util.Map;

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
        return saved.getId();
    }

    public Page<ReceiveRequestResponseDto> findAllReceiveRequest(TokenClaim tokenClaim, Pageable pageable) {

        User user = getUser(tokenClaim.getUserId());
        Page<ExchangeRequest> exchangeRequestList = exchangeRequestRepository.findAllReceiveRequest(
            user.getId(), pageable);

        return exchangeRequestList.map(ReceiveRequestResponseDto::from);
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
            DirectRoomCreateResponseDto room = directRoomService.createRoom(exchangeRequestId, user.getId());
            return ExchangeRoomResponseDto.from(room);
        }

        if (exchangeRequest.getStatus() == ExchangeStatus.REJECTED) {
            exchangeRequestRepository.delete(exchangeRequest);
        }

        return null;
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

        return requests.map(ReceiveRequestResponseDto::from);
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
