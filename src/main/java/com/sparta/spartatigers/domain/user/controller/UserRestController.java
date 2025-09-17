package com.sparta.spartatigers.domain.user.controller;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.user.dto.UserRegisterRequest;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.service.UserService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @PostMapping("/api/v1/users")
    public ApiResponse<?> register(@RequestBody @Valid UserRegisterRequest request) {
        User user = request.toDomain();
        userService.addUser(user);

        return ApiResponse.success(user);
    }

    @GetMapping("/api/v1/users/me")
    public ApiResponse<TokenClaim> me(@Auth TokenClaim tokenClaim) {
        return ApiResponse.success(tokenClaim);
    }
}