package com.sparta.spartatigers.domain.foundation.baseball.match.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.foundation.baseball.match.dto.MatchScheduleResponseDto;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LeagueType;
import com.sparta.spartatigers.domain.foundation.baseball.match.service.MatchScheduleService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 경기 일정 관련 API 컨트롤러
 * 
 * Why: 경기 일정 조회 기능을 liveboard 도메인의 표준 5계층 아키텍처로 통합함.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Validated
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
            @RequestParam @Min(1900) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam(required = false) LeagueType leagueType,
            @Auth TokenClaim tokenClaim) {
        log.info("Fetching monthly schedule: year={}, month={}, leagueType={}", year, month, leagueType);
        Long userId = tokenClaim.getUserId();
        List<MatchScheduleResponseDto> schedule = matchScheduleService.getMonthlySchedule(userId, year, month, leagueType);
        return ApiResponse.success(schedule);
    }
}
