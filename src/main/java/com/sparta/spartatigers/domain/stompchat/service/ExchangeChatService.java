package com.sparta.spartatigers.domain.stompchat.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.directRoom.dto.request.ChatMessageRequest;
import com.sparta.spartatigers.domain.directRoom.dto.response.RedisMessage;
import com.sparta.spartatigers.domain.directRoom.model.DirectMessage;
import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.registry.RedisUserSessionRegistry;
import com.sparta.spartatigers.domain.directRoom.repository.DirectMessageRepository;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.stompchat.pubsub.RedisDirectMessagePublisher;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.util.RedisRateLimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeChatService {

    // TODO: 운영 단계에서는 조정
    private static final int MESSAGE_LIMIT = 10;
    private static final Duration LIMIT_DURATION = Duration.ofSeconds(2);

    private final DirectRoomRepository directRoomRepository;
    private final UserRepository userRepository;
    private final DirectMessageRepository directMessageRepository;
    private final RedisDirectMessagePublisher redisPublisher;
    private final RedisRateLimiter redisRateLimiter;
    private final RedisUserSessionRegistry sessionRegistry;

    @Transactional
    public void sendMessage(Long senderId, ChatMessageRequest request) {

        // 메세지 연속 전송 제한
        log.info(
            "[sendMessage] 메시지 전송 요청 - senderId: {}, roomId: {}", senderId, request.getRoomId());

        String rateLimitKey = "rate-limit:user:" + senderId;
        boolean isLimited =
            redisRateLimiter.isRateLimited(rateLimitKey, MESSAGE_LIMIT, LIMIT_DURATION);
        if (isLimited) {
            log.warn("[sendMessage] 메시지 전송 레이트 리밋 초과 - senderId: {}", senderId);
            throw new InvalidRequestException(ExceptionCode.TOO_MANY_MESSAGE);
        }

        // 방, 발신자 조회
        Long roomId = request.getRoomId();
        String messageText = request.getMessage();

        DirectRoom room = directRoomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.warn("[sendMessage] 채팅방 없음 - roomId: {}", roomId);
                    return new InvalidRequestException(ExceptionCode.CHATROOM_NOT_FOUND);});

        if (room.isCompleted()) {
            log.warn("[sendMessage] 완료된 채팅방 전송 차단 - roomId: {}, senderId: {}", roomId, senderId);
            throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> {
                        log.warn("[sendMessage] 사용자 없음 - senderId: {}", senderId);
                        return new InvalidRequestException(ExceptionCode.USER_NOT_FOUND);});

        // DB에 메세지 저장 (UNREAD 상태) -> 알아서 flush 됨
        DirectMessage savedMessage = directMessageRepository.save(DirectMessage.of(room, sender, messageText));

        // redis 발행 (UNREAD 상태)
        redisPublisher.publish("directRoom:" + roomId, RedisMessage.from(savedMessage));
        log.info("[1:1 채팅] PUBLISH / roomId={} , messageId={}", roomId, savedMessage.getId());

        // 수신자 조회
        Long receiverId = getOpponentId(room, senderId);

        // 수신자가 접속중이면 읽음 처리
        boolean receiverOnline = sessionRegistry.isUserInRoom(roomId, receiverId);
        if (receiverOnline) {
            savedMessage.markAsRead();
            directMessageRepository.save(savedMessage);
            RedisMessage readStatusMessage = RedisMessage.readStatus(savedMessage.getId(), roomId, true);
            redisPublisher.publish("directRoom:" + roomId, readStatusMessage);
            log.info("[1:1 채팅] REPUBLISH / roomId={} , messageId={} , read={}", roomId, readStatusMessage.getMessageId(), readStatusMessage.isRead());
        }

    }

    // 수신자 찾기 (sender = 발신자임) (room의 sender, receiver는 의미 없음 그냥 유저 1,2)
    public Long getOpponentId (DirectRoom room, Long senderId) {
        if (room.getSender().getId().equals(senderId)) {
            return room.getReceiver().getId();
        } else if (room.getReceiver().getId().equals(senderId)) {
            return room.getSender().getId();
        } else {
            throw new InvalidRequestException(ExceptionCode.USER_NOT_FOUND);
        }
    }


}