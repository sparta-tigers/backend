package com.sparta.spartatigers.domain.weather.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

	private final WeatherService weatherService;

}