package com.sparta.spartatigers.domain.weather.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;
import com.sparta.spartatigers.domain.liveboard.match.model.Stadium;
import com.sparta.spartatigers.domain.weather.model.WindDirection;

import lombok.Getter;

@Getter
public class NowCastResponseDto {

	private LocalDateTime referenceTime; // 업데이트 시간
	private String stadium;

	// ✅ 현재 날씨는 최대한 초단기 실황을 활용할 것
	// 🌤️️
	private Double temperature; // 기온(T1H) - 초단기실황
	private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보
	// ️ ☔
	private RainType rainType; // 강수형태(PYT) - 초단기실황
	private Double rainAmount; // 강수량(RN1) - 초단기실황
	private Integer rainProbability; // 강수확률(POP) - 단기예보 (null 허용)
	// 💨
	private Double windSpeed; // 풍속(WSD) - 초단기실황
	private WindDirection windDirection; // 풍향(VEC) - 초단기실황

	public static NowCastResponseDto of(
			LocalDateTime referenceTime,
			Stadium stadium,
			Double temperature,
			SkyStatus skyStatus,
			RainType rainType,
			Double rainAmount,
			Integer rainProbability,
			Double windSpeed,
			WindDirection windDirection) {
		NowCastResponseDto dto = new NowCastResponseDto();
		dto.referenceTime = referenceTime;
		dto.stadium = stadium.getName();
		// NaN은 JSON 직렬화 시 문제가 되므로 null로 정규화
		dto.temperature = (temperature == null || temperature.isNaN()) ? null : temperature;
		dto.skyStatus = skyStatus;
		dto.rainType = rainType;
		dto.rainAmount = (rainAmount == null || rainAmount.isNaN()) ? null : rainAmount;
		dto.rainProbability = rainProbability;
		dto.windSpeed = (windSpeed == null || windSpeed.isNaN()) ? null : windSpeed;
		dto.windDirection = windDirection;
		return dto;
	}

	public static NowCastResponseDto empty(Stadium stadium, LocalDateTime referenceTime) {
		NowCastResponseDto dto = new NowCastResponseDto();
		dto.referenceTime = referenceTime;
		dto.stadium = stadium.getName();
		return dto;
	}
}
