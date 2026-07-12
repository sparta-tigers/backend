package com.sparta.spartatigers.domain.support.weather.model;

import lombok.Getter;

@Getter
public class Weather {

    private double temperature; // 기온(TMP)
    private SkyStatus skyStatus; // 하늘상태(SKY)
    private double windSpeed; // 풍속(WSD)
    private WindDirection windDirection; // 풍향(VEC)
    private double rainAmount; // 강수량(RN1/PCP)
    private int rainProbability; // 강수확률(POP)
    private RainType rainType; // 강수형태(PYT)
}
