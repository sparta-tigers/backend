package com.sparta.spartatigers.global.firebase.controller;

import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.foundation.user.account.service.UserService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.firebase.dto.FcmTokenRequest;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FCMController {

    private final UserService userService;

    @PostMapping("/fcm-token")
    public ApiResponse<Void> saveFcmToken(@Auth TokenClaim tokenClaim, @Valid @RequestBody FcmTokenRequest request) {
        userService.updateFcmToken(tokenClaim, request);
        return ApiResponse.success(null);
    }
}