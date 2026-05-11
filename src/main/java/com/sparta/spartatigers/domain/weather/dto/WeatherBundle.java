package com.sparta.spartatigers.domain.weather.dto;

import java.util.List;

/**
 * NowCast + ForeCast 묶음 전달 객체
 *
 * Why: WeatherService.getNowCastAndForeCast()가 두 결과를 한 번에 반환하기 위한
 * 단순 레코드. 기상청 API 3회 호출 결과를 공유해 중복 호출을 방지한다.
 */
public record WeatherBundle(
        NowCastResponseDto nowCast,
        List<ForeCastResponseDto> foreCast) {
}
