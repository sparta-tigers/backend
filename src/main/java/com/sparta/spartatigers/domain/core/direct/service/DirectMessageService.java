package com.sparta.spartatigers.domain.core.direct.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.core.direct.dto.response.DirectRoomMessageResponse;
import com.sparta.spartatigers.domain.core.direct.model.DirectMessage;
import com.sparta.spartatigers.domain.core.direct.model.DirectRoom;
import com.sparta.spartatigers.domain.core.direct.repository.DirectMessageRepository;
import com.sparta.spartatigers.domain.core.direct.repository.DirectRoomRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageService {

        private final DirectRoomRepository directRoomRepository;
        private final DirectMessageRepository directRoomMessageRepository;

        @Transactional
        public Page<DirectRoomMessageResponse> getMessages(
                        Long roomId, Long userId, Pageable pageable) {
                log.info("[getMessages] 메시지 목록 조회 시작 - roomId: {}, userId: {}", roomId, userId);
                DirectRoom room = directRoomRepository
                                .findById(roomId)
                                .orElseThrow(
                                                () -> {
                                                        log.warn("[getMessages] 채팅방 없음 - roomId: {}", roomId);
                                                        return new InvalidRequestException(
                                                                        ExceptionCode.CHATROOM_NOT_FOUND);
                                                });

                if (!room.getSender().getId().equals(userId)
                                && !room.getReceiver().getId().equals(userId)) {
                        log.warn("[getMessages] 권한 없는 유저의 접근 시도 - roomId: {}, userId: {}", roomId, userId);
                        throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
                }

                // 메세지 조회 전 안읽은 메세지 일괄 읽음 처리
                List<DirectMessage> unreadMessages = directRoomMessageRepository.findUnreadMsg(roomId, userId);
                unreadMessages.forEach(DirectMessage::markAsRead);

                // 메세지 조회
                Page<DirectRoomMessageResponse> messages = directRoomMessageRepository
                                .findByDirectRoomIdWithSender(roomId, pageable)
                                .map(m -> DirectRoomMessageResponse.from(m));

                log.info(
                                "[getMessages] 메시지 조회 완료 - roomId: {}, 총 개수: {}",
                                roomId,
                                messages.getTotalElements());
                return messages;
        }
}
