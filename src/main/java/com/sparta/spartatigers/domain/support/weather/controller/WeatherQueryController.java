package com.sparta.spartatigers.domain.support.weather.controller;

import com.sparta.spartatigers.domain.support.weather.dto.MatchWeatherResponseDto;
import com.sparta.spartatigers.domain.support.weather.service.WeatherQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 라이브보드 룸 구장날씨 조회 REST 컨트롤러
 *
 * Why: 프론트엔드 라이브보드 룸의 "구장날씨" 탭에서 matchId 기반으로
 * NowCast + ForeCast를 단일 호출에 받기 위함.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liveboard")
public class WeatherQueryController {

    private final WeatherQueryService weatherQueryService;

    /**
     * GET /api/liveboard/{matchId}/weather
     *
     * matchId로 경기 조회 → 연결된 구장의 현재 날씨와 시간대별 예보를 반환.
     */
    @GetMapping("/{matchId}/weather")
    public MatchWeatherResponseDto getMatchWeather(@PathVariable("matchId") Long matchId) {
        return weatherQueryService.getMatchWeather(matchId);
    }
}
