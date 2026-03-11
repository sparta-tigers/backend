package com.sparta.spartatigers.domain.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.notification.dto.DeviceTokenRequest;
import com.sparta.spartatigers.domain.notification.service.DeviceTokenService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 클라이언트 디바이스(푸시) 토큰 등록용 컨트롤러.
 * 디바이스 토큰을 DB에 영구 저장하고 관리한다.
 */
@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ApiResponse<String> registerDeviceToken(
        @Valid @RequestBody DeviceTokenRequest request,
        @Auth TokenClaim tokenClaim
    ) {
        deviceTokenService.registerDeviceToken(tokenClaim, request);
        
        return ApiResponse.success("디바이스 토큰이 정상적으로 등록되었습니다.");
    }
}

