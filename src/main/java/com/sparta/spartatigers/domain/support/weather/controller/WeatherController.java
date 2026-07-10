package com.sparta.spartatigers.domain.support.weather.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.support.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.support.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.support.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

	private final WeatherService weatherService;

	@GetMapping("/now")
	public NowCastResponseDto getNowCast(@RequestParam("stadiumId") Long stadiumId) {
		return weatherService.getNowCast(stadiumId);
	}

	@GetMapping("/fore")
	public List<ForeCastResponseDto> getForeCast(@RequestParam("stadiumId") Long stadiumId) {
		return weatherService.getForeCast(stadiumId);
	}
}
