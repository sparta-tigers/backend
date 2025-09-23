package com.sparta.spartatigers.domain.Weather.model;

public class Weather {
	private double temperature; //기온(TMP) - 초단기실황, 초단기예보
	private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보, 단기예보
	private int rainProbability; // 강수확률(POP) - 단기예보
	private RainType rainType; // 강수형태(PYT) - 초단기예보
	private double rainAmount; // 강수량(RN1/PCP) - 초단기예보

}
