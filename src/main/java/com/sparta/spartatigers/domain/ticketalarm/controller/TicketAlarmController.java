package com.sparta.spartatigers.domain.ticketalarm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.CreateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.response.TicketAlarmResponseDto;
import com.sparta.spartatigers.domain.ticketalarm.service.TicketAlarmService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticketalarm")
public class TicketAlarmController {

	private final TicketAlarmService ticketAlarmService;

	@PostMapping
	public ApiResponse<TicketAlarmResponseDto> createAlarm(
		@RequestBody CreateTicketAlarmRequestDto request,
		@Auth TokenClaim tokenClaim
	){
		Long userId = tokenClaim.getUserId();
		return ApiResponse.created(ticketAlarmService.createAlarm(userId, request));
	}

	@GetMapping
	public ApiResponse<List<TicketAlarmResponseDto>> getAllAlarms (
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(ticketAlarmService.getAllAlarms(userId));
	}


}
