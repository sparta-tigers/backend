package com.sparta.spartatigers.domain.directRoom.controller;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.directRoom.dto.response.DirectRoomMessageResponse;
import com.sparta.spartatigers.domain.directRoom.service.DirectMessageService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-rooms")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @GetMapping("/{roomId}/messages")
    public ApiResponse<Page<DirectRoomMessageResponse>> getMessages(
            @Auth TokenClaim tokenClaim,
            @PathVariable Long roomId,
            @PageableDefault(
                            size = 30,
                            sort = {"sentAt", "id"},
                            direction = Sort.Direction.DESC)
                    Pageable pageable) {

        Long currentUserId = tokenClaim.getUserId();

        Page<DirectRoomMessageResponse> messages =
                directMessageService.getMessages(roomId, currentUserId, pageable);

        return ApiResponse.ok(messages);
    }
}
