package com.sparta.spartatigers.domain.auth.controller;

import com.sparta.spartatigers.domain.auth.dto.LogoutRequest;
import com.sparta.spartatigers.domain.auth.dto.OauthLoginRequest;
import com.sparta.spartatigers.domain.auth.dto.RefreshRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.dto.UserLoginRequest;
import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.service.AuthService;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ApiResponse<Token> login(@Valid @RequestBody UserLoginRequest request) {
        Token token = authService.login(request.email(), request.password());

        return ApiResponse.success(token);
    }

    @PostMapping("/api/auth/oauth/kakao")
    public ApiResponse<Token> kakaoLogin(@Valid @RequestBody OauthLoginRequest request) {
        Token token = authService.kakaoLogin(request.code(), request.redirectUri());

        return ApiResponse.success(token);
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());

        return ApiResponse.success(null);
    }

    @PostMapping("/api/auth/refresh")
    public ApiResponse<Token> refresh(@Valid @RequestBody RefreshRequest request) {
        Token token = authService.refresh(request.refreshToken());

        return ApiResponse.success(token);
    }
}
