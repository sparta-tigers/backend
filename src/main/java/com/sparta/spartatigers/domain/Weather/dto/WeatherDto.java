package com.sparta.spartatigers.domain.Weather.dto;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.Weather.model.RainType;
import com.sparta.spartatigers.domain.Weather.model.SkyStatus;
import com.sparta.spartatigers.domain.team.model.Stadium;

public class WeatherDto {

	private LocalDateTime forecastTime;
	private Stadium stadium;

	private double temperature; //기온(TMP) - 초단기실황, 초단기예보
	private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보, 단기예보
	private int rainProbability; // 강수확률(POP) - 단기예보
	private RainType rainType; // 강수형태(PYT) - 초단기예보
	private double rainAmount; // 강수량(RN1/PCP) - 초단기예보

}
