package com.sparta.spartatigers.domain.auth.controller;

import com.sparta.spartatigers.domain.auth.dto.UserLoginRequest;
import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.service.AuthService;
import com.sparta.spartatigers.domain.user.model.LoginUser;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/login")
    public ApiResponse<Token> login(@Valid @RequestBody UserLoginRequest request, HttpSession session) {
        Token token = authService.login(request.email(), request.password());

        return ApiResponse.ok(token);
    }
}
