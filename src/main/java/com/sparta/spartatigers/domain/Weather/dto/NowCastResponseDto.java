package com.sparta.spartatigers.domain.weather.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;
import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.weather.model.WindDirection;

public class NowCastResponseDto {

	private LocalDateTime referenceTime; // 업데이트 시간
	private Stadium stadium;

	// ✅ 현재 날씨는 최대한 초단기 실황을 활용할 것
	// 🌤️️
	private double temperature; // 기온(T1H) - 초단기실황
	private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보
	//️ ☔
	private RainType rainType; // 강수형태(PYT) - 초단기실황
	private double rainAmount; // 강수량(RN1) - 초단기실황
	// 💨
	private double windSpeed; // 풍속(WSD) - 초단기실황
	private WindDirection windDirection; // 풍향(VEC) - 초단기실황

	public static NowCastResponseDto of(
		LocalDateTime referenceTime,
		Stadium stadium,
		double temperature,
		SkyStatus skyStatus,
		RainType rainType,
		double rainAmount,
		double windSpeed,
		WindDirection windDirection
	) {
		NowCastResponseDto dto = new NowCastResponseDto();
		dto.referenceTime = referenceTime;
		dto.stadium = stadium;
		dto.temperature = temperature;
		dto.skyStatus = skyStatus;
		dto.rainType = rainType;
		dto.rainAmount = rainAmount;
		dto.windSpeed = windSpeed;
		dto.windDirection = windDirection;
		return dto;
	}
}
