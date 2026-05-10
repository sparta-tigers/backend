package com.sparta.spartatigers.domain.liveboard.dto;

import java.util.Collections;
import java.util.List;

import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;

import lombok.Builder;
import lombok.Getter;

/**
 * matchId 기반 구장 날씨 조회 API 응답 DTO
 *
 * Why: 프론트엔드가 matchId 하나로 NowCast + ForeCast를 단일 호출에 받기 위함.
 * 프론트엔드의 MatchWeatherDto와 1:1 매핑.
 */
@Getter
@Builder
public class MatchWeatherResponse {

    private final String stadiumName;
    private final NowCastResponseDto nowCast;
    private final List<ForeCastResponseDto> foreCast;

    public static MatchWeatherResponse of(
            String stadiumName,
            NowCastResponseDto nowCast,
            List<ForeCastResponseDto> foreCast) {
        return MatchWeatherResponse.builder()
                .stadiumName(stadiumName)
                .nowCast(nowCast)
                .foreCast(foreCast != null ? foreCast : Collections.emptyList())
                .build();
    }
}
