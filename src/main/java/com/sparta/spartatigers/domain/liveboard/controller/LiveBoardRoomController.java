package com.sparta.spartatigers.domain.liveboard.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.sparta.spartatigers.domain.liveboard.dto.LiveBoardRoomResponseDto;
import com.sparta.spartatigers.domain.liveboard.service.LiveboardRoomService;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liveboard/room")
public class LiveBoardRoomController {

	private final LiveboardRoomService liveboardRoomService;
	private final Clock clock;

	@PostMapping
	public ApiResponse<String> createRoomsForDay(
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday) {
		if (anyday == null) {
			anyday = LocalDate.now(clock);
		}
		return ApiResponse.created(liveboardRoomService.createRoomsForDay(anyday));
	}

	@GetMapping
	public List<LiveBoardRoomResponseDto> getRoomsForDay(
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday) {
		if (anyday == null) {
			anyday = LocalDate.now(clock);
		}
		return liveboardRoomService.getRoomsForDay(anyday);
	}

	@GetMapping("/{matchId}")
	public ApiResponse<LiveBoardRoomResponseDto> getRoomByMatchId(@PathVariable Long matchId) {
		return ApiResponse.success(liveboardRoomService.getRoomByMatchId(matchId));
	}

	@DeleteMapping
	public ApiResponse<String> deleteRoomsForDay(
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday) {
		if (anyday == null) {
			anyday = LocalDate.now(clock);
		}
		return ApiResponse.success(liveboardRoomService.deleteRoomsForDay(anyday));
	}
}
