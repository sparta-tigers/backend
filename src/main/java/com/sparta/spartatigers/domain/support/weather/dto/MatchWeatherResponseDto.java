package com.sparta.spartatigers.domain.support.weather.dto;

import com.sparta.spartatigers.domain.support.weather.model.WeatherApiStatus;
import java.util.List;

public record MatchWeatherResponseDto(
    String stadiumName,
    WeatherApiStatus status,
    NowCastResponseDto nowCast,
    List<ForeCastResponseDto> foreCast
) {
    public static MatchWeatherResponseDto of(
        String stadiumName,
        WeatherApiStatus status,
        NowCastResponseDto nowCast,
        List<ForeCastResponseDto> foreCast
    ) {
        return new MatchWeatherResponseDto(stadiumName, status, nowCast, foreCast);
    }
}
