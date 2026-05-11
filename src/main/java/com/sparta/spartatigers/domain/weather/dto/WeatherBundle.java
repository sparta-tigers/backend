package com.sparta.spartatigers.domain.weather.dto;

import java.util.List;

import com.sparta.spartatigers.domain.weather.model.WeatherApiStatus;

/**
 * NowCast + ForeCast + 상태 묶음 전달 객체
 *
 * Why: WeatherService.getNowCastAndForeCast()가 두 결과와 함께 기상청 API 상태를
 * 한 번에 반환하기 위한 단순 레코드.
 * status는 세 API 중 가장 심각한 상태를 대표값으로 전달한다.
 */
public record WeatherBundle(
                WeatherApiStatus status,
                NowCastResponseDto nowCast,
                List<ForeCastResponseDto> foreCast) {
}
