package com.sparta.spartatigers.domain.ticketalarm.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.CreateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.UpdateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.response.TicketAlarmResponseDto;
import com.sparta.spartatigers.domain.ticketalarm.service.TicketAlarmService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticketalarm")
public class TicketAlarmController {

	private final TicketAlarmService ticketAlarmService;

	@PostMapping
	public ApiResponse<TicketAlarmResponseDto> createAlarm(
		@Valid @RequestBody CreateTicketAlarmRequestDto request,
		@Auth TokenClaim tokenClaim
	){
		Long userId = tokenClaim.getUserId();
		return ApiResponse.created(ticketAlarmService.createAlarm(userId, request));
	}

	@GetMapping
	public ApiResponse<Page<TicketAlarmResponseDto>> getAllAlarms (
		@Auth TokenClaim tokenClaim,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(ticketAlarmService.getAllAlarms(userId, page, size));
	}

	@PatchMapping ("/{alarmId}")
	public ApiResponse<TicketAlarmResponseDto> updateAlarm (
		@PathVariable Long alarmId,
		@Valid @RequestBody UpdateTicketAlarmRequestDto request,
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(ticketAlarmService.updateAlarm(userId, alarmId, request));

	}

	@DeleteMapping ("/{alarmId}")
	public ApiResponse<?> deleteAlarm (
		@PathVariable Long alarmId,
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		ticketAlarmService.deleteAlarm(userId, alarmId);
		return ApiResponse.success("");
	}

}
