package com.sparta.spartatigers.domain.weather.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.StadiumWeatherRequestDto;
import com.sparta.spartatigers.domain.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

	private final WeatherService weatherService;

	@GetMapping ("/now")
	public NowCastResponseDto getNowCast(@RequestBody StadiumWeatherRequestDto request) {

		return weatherService.getNowCast(request.getStadiumId());
	}

	@GetMapping("/fore")
	public List<ForeCastResponseDto> getForeCast (@RequestBody StadiumWeatherRequestDto request) {

		return weatherService.getForeCast(request.getStadiumId());
	}

}