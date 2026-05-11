package com.sparta.spartatigers.domain.weather.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;

import lombok.Getter;

@Getter
public class ForeCastResponseDto {

	// private LocalDateTime referenceTime; // 업데이트시간
	private LocalDateTime castTime; // 해당되는 예보 시간
	private String stadium;

	// ✅ 미래 날씨는 초단기 실황 활용 X
	// 🌤️️
	private Double temperature; // 기온(T1H) - 초단기예보
	private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보
	// ️ ☔
	private Integer rainProbability; // 강수확률(POP) - 단기예보
	private RainType rainType; // 강수형태(PYT) - 초단기예보
	private Double rainAmount; // 강수량(RN1) - 초단기예보

	public static ForeCastResponseDto of(
			LocalDateTime castTime,
			Stadium stadium,
			Double temperature,
			SkyStatus skyStatus,
			Integer rainProbability,
			RainType rainType,
			Double rainAmount) {
		ForeCastResponseDto dto = new ForeCastResponseDto();
		dto.castTime = castTime;
		dto.stadium = stadium.getName();
		// NaN은 JSON 직렬화 시 문제가 되므로 null로 정규화
		dto.temperature = (temperature == null || temperature.isNaN()) ? null : temperature;
		dto.skyStatus = skyStatus;
		dto.rainProbability = rainProbability;
		dto.rainType = rainType;
		dto.rainAmount = (rainAmount == null || rainAmount.isNaN()) ? null : rainAmount;
		return dto;
	}
}
