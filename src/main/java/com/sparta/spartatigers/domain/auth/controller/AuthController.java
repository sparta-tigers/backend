package com.sparta.spartatigers.domain.auth.controller;

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

    @PostMapping("/api/v1/auth/login")
    public ApiResponse<Token> login(@Valid @RequestBody UserLoginRequest request) {
        Token token = authService.login(request.email(), request.password());

        return ApiResponse.success(token);
    }
}
