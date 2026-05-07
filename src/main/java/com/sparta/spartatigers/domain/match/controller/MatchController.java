package com.sparta.spartatigers.domain.match.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.match.dto.MatchScheduleResponseDto;
import com.sparta.spartatigers.domain.match.service.MatchScheduleService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchScheduleService matchScheduleService;

    /**
     * 월별 경기 일정 조회 API
     * 
     * @param year       조회 연도 (예: 2026)
     * @param month      조회 월 (1-12)
     * @param tokenClaim 인증된 사용자 정보
     * @return 특정 월의 경기 일정 리스트
     */
    @GetMapping("/schedule")
    public ApiResponse<List<MatchScheduleResponseDto>> getMonthlySchedule(
            @RequestParam int year,
            @RequestParam int month,
            @Auth TokenClaim tokenClaim
    ) {
        Long userId = tokenClaim.getUserId();
        List<MatchScheduleResponseDto> schedule = matchScheduleService.getMonthlySchedule(userId, year, month);
        return ApiResponse.success(schedule);
    }
}
