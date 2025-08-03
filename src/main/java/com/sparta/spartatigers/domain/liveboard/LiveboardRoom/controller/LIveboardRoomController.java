package com.sparta.spartatigers.domain.liveboard.LiveboardRoom.controller;

import org.springframework.web.bind.annotation.RestController;
import com.sparta.spartatigers.domain.liveboard.LiveboardRoom.service.LiveboardRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LIveboardRoomController {

	private final LiveboardRoomService liveboardRoomService;


}
