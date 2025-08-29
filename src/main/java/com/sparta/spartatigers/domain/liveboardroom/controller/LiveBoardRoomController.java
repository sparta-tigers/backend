package com.sparta.spartatigers.domain.liveboardroom.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.liveboardroom.dto.LiveBoardRoomResponseDto;
import com.sparta.spartatigers.domain.liveboardroom.service.LiveboardRoomService;
import com.sparta.spartatigers.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liveboard/room")
public class LiveBoardRoomController {

	private final LiveboardRoomService liveboardRoomService;

	@PostMapping
	public void createRoomsForWeek(
		@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate anyday) {
		if (anyday == null) {
			anyday = LocalDate.now();
		}
		liveboardRoomService.createRoomsForWeek(anyday);
	}



}
