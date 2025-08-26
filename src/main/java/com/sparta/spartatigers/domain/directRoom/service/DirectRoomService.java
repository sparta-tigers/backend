package com.sparta.spartatigers.domain.directRoom.service;

import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomCreateResponseDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomResponseDto;
import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.directRoom.repository.DirectRoomRepository;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.stompchat.repository.ExchangeChatRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectRoomService {

    // private final ExchangeRequestRepository exchangeRequestRepository;
//     private final DirectRoomRepository directRoomRepository;
//     private final ExchangeChatRepository exchangeChatRepository;
//     private final UserConnectService userConnectService;
//
//     @Transactional
//     public DirectRoomCreateResponseDto createRoom(Long exchangeRequestId, Long currentUserId) {
//         log.info(
//             "[createRoom] 교환요청 기반 채팅방 생성 시도 - exchangeRequestId: {}, currentUserId: {}",
//             exchangeRequestId,
//             currentUserId);
//         ExchangeRequest exchangeRequest =
//             // exchangeRequestRepository.findByIdOrElseThrow(exchangeRequestId);
//
//         // 권한 확인: 요청한 사람이 교환 요청의 sender 또는 receiver여야 함
//         // if (!exchangeRequest.getSender().getId().equals(currentUserId)
//         //     && !exchangeRequest.getReceiver().getId().equals(currentUserId)) {
//             log.warn(
//                 "[createRoom] 권한 없음 - 요청자 ID: {}, 교환요청 sender: {}, receiver: {}",
//                 currentUserId,
//                 exchangeRequest.getSender().getId(),
//                 exchangeRequest.getReceiver().getId());
//             throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
//         }
//
//         // sender/receiver는 교환 요청 그대로
//         User sender = exchangeRequest.getSender();
//         User receiver = exchangeRequest.getReceiver();
//
//         DirectRoom room =
//             directRoomRepository
//                 .findByExchangeRequest(exchangeRequest)
//                 .orElseGet(
//                     () ->
//                         directRoomRepository.save(
//                             DirectRoom.create(
//                                 exchangeRequest, sender, receiver)));
//
//         log.info("[createRoom] 채팅방 생성 완료 - roomId: {}", room.getId());
//         return DirectRoomCreateResponseDto.from(room);
//     }
//
//     public Page<DirectRoomResponseDto> getRoomsForUser(Long currentUserId, Pageable pageable) {
//         log.info("[getRoomsForUser] 채팅방 목록 조회 - 사용자 ID: {}", currentUserId);
//         return directRoomRepository
//             .findBySenderIdOrReceiverIdWithUsersAndItem(currentUserId, pageable)
//             .map(
//                 room -> {
//                     // 현재 로그인한 유저의 상대방 id 가져옴
//                     Long opponentId =
//                         room.getSender().getId().equals(currentUserId)
//                             ? room.getReceiver().getId()
//                             : room.getSender().getId();
//
//                     boolean isOnline =
//                         userConnectService.isUserOnline(currentUserId, opponentId);
//                     log.debug(
//                         "[getRoomsForUser] 채팅방 ID: {}, 상대방 ID: {}, 상대방 온라인 여부: {}",
//                         room.getId(),
//                         opponentId,
//                         isOnline);
//
//                     return DirectRoomResponseDto.from(room, currentUserId, isOnline);
//                 });
//     }
//
//     // 유저가 직접 채팅방을 삭제할 수도 있음
//     // TODO: 교환 완료 시 채팅방이 readOnly 상태로 바뀌고 6시간 후 자동 삭제 (확장 기능)
//     @Transactional
//     public void deleteRoom(Long directRoomId, Long currentUserId) {
//         log.info("[deleteRoom] 채팅방 삭제 요청 - roomId: {}, userId: {}", directRoomId, currentUserId);
//
//         DirectRoom room =
//             directRoomRepository
//                 .findById(directRoomId)
//                 .orElseThrow(
//                     () -> {
//                         log.warn("[deleteRoom] 채팅방 존재하지 않음 - roomId: {}", directRoomId);
//                         return new InvalidRequestException(
//                             ExceptionCode.CHATROOM_NOT_FOUND);
//                     });
//
//         boolean isSender = room.getSender().getId().equals(currentUserId);
//         boolean isReceiver = room.getReceiver().getId().equals(currentUserId);
//
//         if (!isSender && !isReceiver) {
//             log.warn("[deleteRoom] 삭제 권한 없음 - userId: {}, roomId: {}", currentUserId, directRoomId);
//             throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
//         }
//
//         exchangeChatRepository.deleteAllByDirectRoomId(room.getId());
//         directRoomRepository.delete(room);
//         log.info("[deleteRoom] 채팅방 삭제 완료 - roomId: {}", directRoomId);
//     }
//
//     // 교환 완료 시 호출되며 채팅방이 삭제되는 로직
//     @Transactional
//     public void deleteRoomByExchangeRequestId(Long exchangeRequestId) {
//         log.info(
//             "[deleteRoomByExchangeRequestId] 교환요청 기반 채팅방 삭제 - exchangeRequestId: {}",
//             exchangeRequestId);
//         DirectRoom room =
//             directRoomRepository
//                 .findByExchangeRequestId(exchangeRequestId)
//                 .orElseThrow(
//                     () -> {
//                         log.warn(
//                             "[deleteRoomByExchangeRequestId] 채팅방 존재하지 않음 - exchangeRequestId: {}",
//                             exchangeRequestId);
//                         return new InvalidRequestException(
//                             ExceptionCode.CHATROOM_NOT_FOUND);
//                     });
//
//         exchangeChatRepository.deleteAllByDirectRoomId(room.getId());
//         directRoomRepository.delete(room);
//         log.info("[deleteRoomByExchangeRequestId] 채팅방 삭제 완료 - roomId: {}", room.getId());
//     }
}
