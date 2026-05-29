package com.sparta.spartatigers.domain.directRoom.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomMessageResponse;
import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.directRoom.dto.request.CreateDirectRoomRequestDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomCreateResponseDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomItemResponseDto;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomResponseDto;
import com.sparta.spartatigers.domain.directRoom.service.DirectRoomService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/direct-rooms")
@RequiredArgsConstructor
public class DirectRoomController {
    private final DirectRoomService directRoomService;

    @PostMapping
    public ApiResponse<DirectRoomCreateResponseDto> createDirectRoom(
            @Valid @RequestBody CreateDirectRoomRequestDto request,
            @Auth TokenClaim tokenClaim) {
        Long userId = tokenClaim.getUserId();
        DirectRoomCreateResponseDto directRoomDto = directRoomService.createRoom(request.getExchangeRequestId(),
                userId);
        return ApiResponse.created(directRoomDto);
    }

    @GetMapping
    public ApiResponse<Page<DirectRoomResponseDto>> getDirectRooms(
            @Auth TokenClaim tokenClaim,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long currentUserId = tokenClaim.getUserId();
        Page<DirectRoomResponseDto> rooms = directRoomService.getRoomsForUser(currentUserId, pageable);
        return ApiResponse.success(rooms);
    }

    @GetMapping("/{directRoomId}/item")
    public ApiResponse<DirectRoomItemResponseDto> getDirectRoomItem(
            @PathVariable Long directRoomId,
            @Auth TokenClaim tokenClaim) {
        Long currentUserId = tokenClaim.getUserId();
        DirectRoomItemResponseDto response = directRoomService.getRoomItem(directRoomId, currentUserId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{directRoomId}")
    public ApiResponse<String> deleteDirectRoom(
            @PathVariable Long directRoomId,
            @Auth TokenClaim tokenClaim) {
        Long currentUserId = tokenClaim.getUserId();
        directRoomService.deleteRoom(directRoomId, currentUserId);
        return ApiResponse.success("채팅방이 정상적으로 삭제되었습니다!");
    }

    @GetMapping("/{directRoomId}/messages/after")
    public ApiResponse<List<DirectRoomMessageResponse>> getMessagesAfter(
            @PathVariable Long directRoomId,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime timestamp,
            @Auth TokenClaim tokenClaim,
            @PageableDefault(size = 100, sort = "sentAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Long currentUserId = tokenClaim.getUserId();
        List<DirectRoomMessageResponse> messages = directRoomService.getMessagesAfter(directRoomId, timestamp, currentUserId, pageable);
        return ApiResponse.success(messages);
    }
}
