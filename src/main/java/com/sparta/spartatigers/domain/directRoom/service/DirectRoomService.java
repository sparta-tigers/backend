package com.sparta.spartatigers.domain.directRoom.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomCreateResponseDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomItemResponseDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomMessageResponse;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomResponseDto;
import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.repository.DirectMessageRepository;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.exchangerequest.repository.ExchangeRequestRepository;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectRoomService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final DirectRoomRepository directRoomRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserConnectService userConnectService;

    @Transactional
    public DirectRoomCreateResponseDto createRoom(Long exchangeRequestId, Long currentUserId) {
        log.info(
                "[createRoom] 교환요청 기반 채팅방 생성 시도 - exchangeRequestId: {}, currentUserId: {}",
                exchangeRequestId,
                currentUserId);
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findByIdOrElseThrow(exchangeRequestId);

        // 권한 확인: 요청한 사람이 교환 요청의 sender 또는 receiver여야 함
        if (!exchangeRequest.getSender().getId().equals(currentUserId)
                && !exchangeRequest.getReceiver().getId().equals(currentUserId)) {
            log.warn(
                    "[createRoom] 권한 없음 - 요청자 ID: {}, 교환요청 sender: {}, receiver: {}",
                    currentUserId,
                    exchangeRequest.getSender().getId(),
                    exchangeRequest.getReceiver().getId());
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }

        // sender/receiver는 교환 요청 그대로
        User sender = exchangeRequest.getSender();
        User receiver = exchangeRequest.getReceiver();

        DirectRoom room = directRoomRepository
                .findByExchangeRequest(exchangeRequest)
                .orElseGet(
                        () -> directRoomRepository.save(
                                DirectRoom.create(
                                        exchangeRequest, sender, receiver)));

        log.info("[createRoom] 채팅방 생성 완료 - roomId: {}", room.getId());
        return DirectRoomCreateResponseDto.from(room);
    }

    @Transactional(readOnly = true)
    public Page<DirectRoomResponseDto> getRoomsForUser(Long currentUserId, Pageable pageable) {
        log.info("[getRoomsForUser] 채팅방 목록 조회 - 사용자 ID: {}", currentUserId);
        Page<DirectRoom> rooms = directRoomRepository.findBySenderIdOrReceiverIdWithUsersAndItem(currentUserId,
                pageable);

        if (rooms.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> roomIds = rooms.stream().map(DirectRoom::getId).toList();
        List<Long> opponentIds = rooms.stream()
                .map(room -> room.getSender().getId().equals(currentUserId) ? room.getReceiver().getId()
                        : room.getSender().getId())
                .distinct()
                .toList();

        // Batch fetch unread counts
        List<Object[]> unreadCountsResult = directMessageRepository.countUnreadMsgInBatch(roomIds, currentUserId);
        Map<Long, Long> unreadCountsMap = new HashMap<>();
        for (Object[] row : unreadCountsResult) {
            unreadCountsMap.put((Long) row[0], (Long) row[1]);
        }

        // Batch fetch online statuses
        Map<Long, Boolean> onlineStatusesMap = userConnectService.getOnlineStatuses(opponentIds);

        return rooms.map(
                room -> {
                    Long opponentId = room.getSender().getId().equals(currentUserId)
                            ? room.getReceiver().getId()
                            : room.getSender().getId();

                    boolean isOnline = onlineStatusesMap.getOrDefault(opponentId, false);
                    Long unreadCount = unreadCountsMap.getOrDefault(room.getId(), 0L);

                    log.debug("[getRoomsForUser] 채팅방 ID: {}, 상대방 ID: {}, 상대방 온라인 여부: {}", room.getId(), opponentId,
                            isOnline);
                    return DirectRoomResponseDto.from(room, unreadCount, currentUserId, isOnline);
                });
    }

    @Transactional(readOnly = true)
    public DirectRoomItemResponseDto getRoomItem(Long directRoomId, Long currentUserId) {
        DirectRoom room = directRoomRepository
                .findById(directRoomId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.CHATROOM_NOT_FOUND));

        ExchangeRequest exchangeRequest = room.getExchangeRequest();

        // [FIX] 문제 1: 권한 검사 시에도 room이 아닌 exchangeRequest.getSender() / getReceiver()를
        // 사용하여 추후 발생할 수 있는 검증 불일치 방지 및 대칭성 보장
        boolean isSender = exchangeRequest.getSender().getId().equals(currentUserId);
        boolean isReceiver = exchangeRequest.getReceiver().getId().equals(currentUserId);
        if (!isSender && !isReceiver) {
            throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
        }

        Item item = exchangeRequest.getItem();

        // [FIX] 상대방 정보를 exchangeRequest의 sender/receiver만으로 대칭 처리
        // 기존: sender면 item.getUser()에서 가져왔으나, 아이템 소유자 분리 시 잘못된 상대방 반환 가능
        // 개선: exchangeRequest.getReceiver() == 아이템 소유자임이 항상 보장됨
        Long opponentId;
        String opponentNickname;
        if (currentUserId.equals(exchangeRequest.getSender().getId())) {
            opponentId = exchangeRequest.getReceiver().getId();
            opponentNickname = exchangeRequest.getReceiver().getNickname();
        } else {
            opponentId = exchangeRequest.getSender().getId();
            opponentNickname = exchangeRequest.getSender().getNickname();
        }

        return DirectRoomItemResponseDto.from(item, exchangeRequest.getStatus().name(), opponentId, opponentNickname);
    }

    // 유저가 직접 채팅방을 삭제할 수도 있음
    // TODO: 교환 완료 시 채팅방이 readOnly 상태로 바뀌고 6시간 후 자동 삭제 (확장 기능)
    @Transactional
    public void deleteRoom(Long directRoomId, Long currentUserId) {
        log.info("[deleteRoom] 채팅방 삭제 요청 - roomId: {}, userId: {}", directRoomId, currentUserId);

        DirectRoom room = directRoomRepository
                .findById(directRoomId)
                .orElseThrow(
                        () -> {
                            log.warn("[deleteRoom] 채팅방 존재하지 않음 - roomId: {}", directRoomId);
                            return new InvalidRequestException(
                                    ExceptionCode.CHATROOM_NOT_FOUND);
                        });

        boolean isSender = room.getSender().getId().equals(currentUserId);
        boolean isReceiver = room.getReceiver().getId().equals(currentUserId);

        if (!isSender && !isReceiver) {
            log.warn("[deleteRoom] 삭제 권한 없음 - userId: {}, roomId: {}", currentUserId, directRoomId);
            throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
        }

        directMessageRepository.deleteAllByDirectRoomId(room.getId());
        directRoomRepository.delete(room);
        log.info("[deleteRoom] 채팅방 삭제 완료 - roomId: {}", directRoomId);
    }

    // 교환 완료 시 호출되며 채팅방이 삭제되는 로직
    @Transactional
    public void deleteRoomByExchangeRequestId(Long exchangeRequestId) {
        log.info(
                "[deleteRoomByExchangeRequestId] 교환요청 기반 채팅방 삭제 - exchangeRequestId: {}",
                exchangeRequestId);
        DirectRoom room = directRoomRepository
                .findByExchangeRequestId(exchangeRequestId)
                .orElseThrow(
                        () -> {
                            log.warn(
                                    "[deleteRoomByExchangeRequestId] 채팅방 존재하지 않음 - exchangeRequestId: {}",
                                    exchangeRequestId);
                            return new InvalidRequestException(
                                    ExceptionCode.CHATROOM_NOT_FOUND);
                        });

        directMessageRepository.deleteAllByDirectRoomId(room.getId());
        directRoomRepository.delete(room);
        log.info("[deleteRoomByExchangeRequestId] 채팅방 삭제 완료 - roomId: {}", room.getId());
    }

    @Transactional(readOnly = true)
    public List<DirectRoomMessageResponse> getMessagesAfter(
            Long roomId, LocalDateTime afterTimestamp, Long currentUserId, Pageable pageable) {
        log.info("[getMessagesAfter] 누락 메시지 조회 - roomId: {}, after: {}, userId: {}", roomId, afterTimestamp,
                currentUserId);

        DirectRoom room = directRoomRepository.findById(roomId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.CHATROOM_NOT_FOUND));

        // 권한 검증
        boolean isSender = room.getSender().getId().equals(currentUserId);
        boolean isReceiver = room.getReceiver().getId().equals(currentUserId);
        if (!isSender && !isReceiver) {
            throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
        }

        return directMessageRepository.findMessagesAfterTimestamp(roomId, afterTimestamp, pageable)
                .stream()
                .map(DirectRoomMessageResponse::from)
                .toList();
    }
}
