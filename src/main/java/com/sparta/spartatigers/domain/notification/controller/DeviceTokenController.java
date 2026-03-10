package com.sparta.spartatigers.domain.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.annotation.Auth;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.notification.dto.DeviceTokenRequest;
import com.sparta.spartatigers.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 클라이언트 디바이스(푸시) 토큰 등록용 컨트롤러.
 * 현재 구현은 토큰 유효성을 간단히 검증하고 서버 로그에 기록만 한다.
 * 추후 DB 저장/멀티 디바이스 지원이 필요해지면 Service/Repository 레이어를 도입한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    @PostMapping
    public ApiResponse<String> registerDeviceToken(
        @Valid @RequestBody DeviceTokenRequest request,
        @Auth TokenClaim tokenClaim
    ) {
        Long userId = tokenClaim.getUserId();

        if (request.token() == null || request.token().isBlank()) {
            return ApiResponse.badRequest("토큰이 비어 있습니다.");
        }

        log.info(
            "[DeviceToken] userId={}, deviceType={}, tokenPrefix={}",
            userId,
            request.deviceType(),
            request.token().substring(0, Math.min(request.token().length(), 20))
        );

        // TODO: 필요 시 DB에 디바이스 토큰 영구 저장 및 중복/만료 관리 추가

        return ApiResponse.success("디바이스 토큰이 정상적으로 등록되었습니다.");
    }
}

