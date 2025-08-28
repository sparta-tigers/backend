package com.sparta.spartatigers.domain.directRoom.service;

import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.registry.RedisUserSessionRegistry;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConnectService {

    private final RedisUserSessionRegistry userSessionRegistry;
    private final DirectRoomRepository directRoomRepository;

    // 채팅방 목록 조회용
    public boolean isUserOnline(Long requesterId, Long targetUserId) {
        log.debug(
            "[isUserOnline] 접속 여부 조회 요청 - requesterId: {}, targetUserId: {}",
            requesterId,
            targetUserId);

        if (!hasChatRoomBetween(requesterId, targetUserId)) {
            log.warn(
                "[isUserOnline] 사용자 간 채팅방 없음 - requesterId: {}, targetUserId: {}",
                requesterId,
                targetUserId);
            throw new InvalidRequestException(ExceptionCode.ACCESS_DENIED);
        }

        boolean isConnected = userSessionRegistry.isUserConnected(targetUserId);
        log.debug("[isUserOnline] targetUserId {} 접속 상태: {}", targetUserId, isConnected);
        return isConnected;
    }

    // user A와 B 사이에 채팅방이 존재하는지 확인하는 로직
    // 채팅방 목록 조회용
    private boolean hasChatRoomBetween(Long userAId, Long userBId) {
        boolean exists =
            directRoomRepository.existsBySenderIdAndReceiverId(userAId, userBId)
                || directRoomRepository.existsBySenderIdAndReceiverId(userBId, userAId);
        log.debug(
            "[hasChatRoomBetween] userAId: {}, userBId: {}, exists: {}",
            userAId,
            userBId,
            exists);
        return exists;
    }

    // 특정 채팅방 참여중인 상대방 유저 접속 여부 조회
    public boolean isOpponentOnlineInRoom(Long requesterId, Long roomId) {
        log.debug(
            "[isOpponentOnlineInRoom] 채팅방 상대방 접속 여부 확인 요청 - requesterId: {}, roomId: {}",
            requesterId,
            roomId);
        DirectRoom room =
            directRoomRepository
                .findById(roomId)
                .orElseThrow(
                    () -> {
                        log.warn(
                            "[isOpponentOnlineInRoom] 채팅방 없음 - roomId: {}", roomId);
                        return new InvalidRequestException(
                            ExceptionCode.CHATROOM_NOT_FOUND);
                    });

        Long opponentId;

        if (requesterId.equals(room.getSender().getId())) {
            opponentId = room.getReceiver().getId();
        } else if (requesterId.equals(room.getReceiver().getId())) {
            opponentId = room.getSender().getId();
        } else {
            log.warn(
                "[isOpponentOnlineInRoom] 요청자 채팅방 참여자 아님 - requesterId: {}, roomId: {}",
                requesterId,
                roomId);
            throw new InvalidRequestException(ExceptionCode.ACCESS_DENIED);
        }

        boolean isConnected = userSessionRegistry.isUserConnected(opponentId);
        log.debug("[isOpponentOnlineInRoom] 상대방 userId: {} 접속 상태: {}", opponentId, isConnected);
        return isConnected;
    }
}
