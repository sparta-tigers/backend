package com.sparta.spartatigers.domain.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.dashboard.dto.HomeDashboardResponseDto;
import com.sparta.spartatigers.domain.dashboard.service.DashboardService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ApiResponse<HomeDashboardResponseDto> getDashboardSummary(@Auth TokenClaim tokenClaim) {
        log.debug("Fetching dashboard summary for user: {}", tokenClaim.getUserId());
        HomeDashboardResponseDto summary = dashboardService.getDashboardSummary(tokenClaim.getUserId());
        return ApiResponse.success(summary);
    }
}
