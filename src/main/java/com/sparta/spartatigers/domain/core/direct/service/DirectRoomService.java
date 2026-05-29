package com.sparta.spartatigers.domain.core.direct.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.core.direct.dto.response.DirectRoomCreateResponseDto;
import com.sparta.spartatigers.domain.core.direct.dto.response.DirectRoomItemResponseDto;
import com.sparta.spartatigers.domain.core.direct.dto.response.DirectRoomMessageResponse;
import com.sparta.spartatigers.domain.core.direct.dto.response.DirectRoomResponseDto;
import com.sparta.spartatigers.domain.core.direct.model.DirectRoom;
import com.sparta.spartatigers.domain.core.direct.repository.DirectMessageRepository;
import com.sparta.spartatigers.domain.core.direct.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.core.direct.repository.TradeQueryDao;
import com.sparta.spartatigers.domain.core.direct.repository.TradeQueryDao.ExchangeUsersInfo;
import com.sparta.spartatigers.domain.core.direct.repository.TradeQueryDao.TradeItemDetailInfo;
import com.sparta.spartatigers.domain.core.direct.repository.TradeQueryDao.TradeItemInfo;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectRoomService {

        private final TradeQueryDao tradeQueryDao;
        private final DirectRoomRepository directRoomRepository;
        private final DirectMessageRepository directMessageRepository;
        private final UserConnectService userConnectService;
        private final UserRepository userRepository;

        @Transactional
        public DirectRoomCreateResponseDto createRoom(Long exchangeRequestId, Long currentUserId) {
                log.info(
                                "[createRoom] 교환요청 기반 채팅방 생성 시도 - exchangeRequestId: {}, currentUserId: {}",
                                exchangeRequestId,
                                currentUserId);
                ExchangeUsersInfo usersInfo = tradeQueryDao.getExchangeUsers(exchangeRequestId);

                // 권한 확인: 요청한 사람이 교환 요청의 sender 또는 receiver여야 함
                if (!usersInfo.senderId().equals(currentUserId)
                                && !usersInfo.receiverId().equals(currentUserId)) {
                        log.warn(
                                        "[createRoom] 권한 없음 - 요청자 ID: {}, 교환요청 sender: {}, receiver: {}",
                                        currentUserId,
                                        usersInfo.senderId(),
                                        usersInfo.receiverId());
                        throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
                }

                // sender/receiver는 교환 요청 그대로
                User sender = userRepository.findById(usersInfo.senderId())
                        .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
                User receiver = userRepository.findById(usersInfo.receiverId())
                        .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));

                DirectRoom room = directRoomRepository
                                .findByExchangeRequestId(exchangeRequestId)
                                .orElseGet(
                                                () -> directRoomRepository.save(
                                                                DirectRoom.create(
                                                                                exchangeRequestId, sender, receiver)));

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

                // [FIX] 문제 2: Object[] 원시 배열 대신 명시적인 Projection 인터페이스 사용 (타입 안정성 확보)
                List<DirectMessageRepository.UnreadCountProjection> unreadCountsResult = directMessageRepository
                                .countUnreadMsgInBatch(roomIds, currentUserId);
                Map<Long, Long> unreadCountsMap = new HashMap<>();
                for (DirectMessageRepository.UnreadCountProjection row : unreadCountsResult) {
                        unreadCountsMap.put(row.getRoomId(), row.getCount());
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

                                        log.debug("[getRoomsForUser] 채팅방 ID: {}, 상대방 ID: {}, 상대방 온라인 여부: {}",
                                                        room.getId(), opponentId,
                                                        isOnline);
                                        TradeItemInfo itemInfo = tradeQueryDao.getTradeItemInfo(room.getExchangeRequestId());
                                        return DirectRoomResponseDto.from(room, itemInfo, room.getExchangeRequestId(), unreadCount, currentUserId, isOnline);
                                });
        }

        @Transactional(readOnly = true)
        public DirectRoomItemResponseDto getRoomItem(Long directRoomId, Long currentUserId) {
                DirectRoom room = directRoomRepository
                                .findById(directRoomId)
                                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.CHATROOM_NOT_FOUND));

                TradeItemDetailInfo detailInfo = tradeQueryDao.getTradeItemDetail(room.getExchangeRequestId());

                // [FIX] 문제 1: 권한 검사 시에도 room이 아닌 exchangeRequest.getSender() / getReceiver()를
                // 사용하여 추후 발생할 수 있는 검증 불일치 방지 및 대칭성 보장
                boolean isSender = detailInfo.senderId().equals(currentUserId);
                boolean isReceiver = detailInfo.receiverId().equals(currentUserId);
                if (!isSender && !isReceiver) {
                        throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
                }

                // [FIX] 상대방 정보를 exchangeRequest의 sender/receiver만으로 대칭 처리
                // 기존: sender면 item.getUser()에서 가져왔으나, 아이템 소유자 분리 시 잘못된 상대방 반환 가능
                // 개선: exchangeRequest.getReceiver() == 아이템 소유자임이 항상 보장됨
                Long opponentId;
                String opponentNickname;
                
                // 프론트의 사용자 정보는 repository로 가져올 수도 있지만 단순 ID만 반환하거나 방의 상대방을 찾는다
                if (currentUserId.equals(detailInfo.senderId())) {
                        opponentId = room.getReceiver().getId();
                        opponentNickname = room.getReceiver().getNickname();
                } else {
                        opponentId = room.getSender().getId();
                        opponentNickname = room.getSender().getNickname();
                }

                return DirectRoomItemResponseDto.from(
                        detailInfo.itemId(), detailInfo.title(), detailInfo.description(),
                        detailInfo.category(), detailInfo.status(),
                        detailInfo.ownerId(), detailInfo.ownerNickname(),
                        detailInfo.exchangeStatus(), opponentId, opponentNickname);
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

                // [FIX] 문제 4: 권한 검사 시에도 room이 아닌 usersInfo.senderId() / receiverId()를
                // 사용하여 getRoomItem 메서드와 검증 기준을 완벽하게 통일 (단일 진실의 원천)
                ExchangeUsersInfo usersInfo = tradeQueryDao.getExchangeUsers(room.getExchangeRequestId());
                boolean isSender = usersInfo.senderId().equals(currentUserId);
                boolean isReceiver = usersInfo.receiverId().equals(currentUserId);
                if (!isSender && !isReceiver) {
                        throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
                }

                return directMessageRepository.findMessagesAfterTimestamp(roomId, afterTimestamp, pageable)
                                .stream()
                                .map(m -> DirectRoomMessageResponse.from(m))
                                .toList();
        }
}
