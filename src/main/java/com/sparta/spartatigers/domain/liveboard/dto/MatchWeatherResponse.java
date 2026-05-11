package com.sparta.spartatigers.domain.liveboard.dto;

import java.util.Collections;
import java.util.List;

import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.model.WeatherApiStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * matchId 기반 구장 날씨 조회 API 응답 DTO
 *
 * Why: 프론트엔드가 matchId 하나로 NowCast + ForeCast를 단일 호출에 받기 위함.
 * 프론트엔드의 MatchWeatherDto와 1:1 매핑.
 *
 * status 필드: 기상청 API 상태를 프론트에 전달해 "점검 중" 등의 메시지를 표시할 수 있게 한다.
 * - SUCCESS: 정상 데이터
 * - NO_DATA: 기상청 데이터 없음 (발표 전, 점검 등)
 * - UPSTREAM_ERROR: 기상청 서버 오류
 * - INTERNAL_ERROR: 우리 서버 호출/파싱 실패
 */
@Getter
@Builder
public class MatchWeatherResponse {

    private final String stadiumName;
    /** 기상청 API 응답 상태 — 프론트가 "데이터 없음/점검 중" 메시지를 표시하는 데 사용 */
    private final WeatherApiStatus status;
    private final NowCastResponseDto nowCast;
    private final List<ForeCastResponseDto> foreCast;

    public static MatchWeatherResponse of(
            String stadiumName,
            WeatherApiStatus status,
            NowCastResponseDto nowCast,
            List<ForeCastResponseDto> foreCast) {
        return MatchWeatherResponse.builder()
                .stadiumName(stadiumName)
                .status(status)
                .nowCast(nowCast)
                .foreCast(foreCast != null ? foreCast : Collections.emptyList())
                .build();
    }

    /** 하위 호환용 — status를 SUCCESS로 고정 */
    public static MatchWeatherResponse of(
            String stadiumName,
            NowCastResponseDto nowCast,
            List<ForeCastResponseDto> foreCast) {
        return of(stadiumName, WeatherApiStatus.SUCCESS, nowCast, foreCast);
    }
}
