package com.sparta.spartatigers.domain.core.direct.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.core.direct.service.UserConnectService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-rooms")
public class UserConnectController {

    private final UserConnectService userConnectService;

    @GetMapping("/{roomId}/user-connect")
    public ApiResponse<Map<String, Object>> getOpponentConnectionStatus(
            @Auth TokenClaim tokenClaim, @PathVariable Long roomId) {
        Long requesterId = tokenClaim.getUserId();

        // 상대방의 접속 여부 확인
        boolean isOpponentOnline = userConnectService.isOpponentOnlineInRoom(requesterId, roomId);

        Map<String, Object> response = Map.of(
                "roomId", roomId,
                "isOpponentOnline", isOpponentOnline);

        return ApiResponse.success(response);
    }
}
