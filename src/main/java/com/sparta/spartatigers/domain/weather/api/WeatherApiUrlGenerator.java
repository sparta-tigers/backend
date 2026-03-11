package com.sparta.spartatigers.domain.weather.api;
import com.sparta.spartatigers.domain.weather.api.ApiTimeCalculator.BaseDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WeatherApiUrlGenerator {

	private final String apiKey;
	public WeatherApiUrlGenerator(
		@Value("${weather.api.key}") String apiKey
	) {
		this.apiKey = apiKey;
	}

	private static final String ULTRA_NCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
	private static final String ULTRA_FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
	private static final String VILAGE_FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

	// 초단기실황 getUltraSrtNcst
	public String getUltraSrtNcstUrl(int nx, int ny) {

		BaseDateTime baseTime = ApiTimeCalculator.getNcstBaseDateTime();

		String urlBuilder = ULTRA_NCST_BASE_URL +
			"?serviceKey=" + apiKey +
			"&numOfRows=" + "10" +// numOfRows(한 페이지 결과수)
		    "&pageNo=" + "1" + // pageNo(페이지 번호)
			"&dataType=JSON" +
			"&base_date=" + baseTime.baseDate() +
			"&base_time=" + baseTime.baseTime() +
			"&nx=" + nx +
			"&ny=" + ny;

		return urlBuilder;
	}

	// 초단기예보 getUltraSrtFcst
	public String getUltraSrtFcstUrl(int nx, int ny)  {

		BaseDateTime baseTime = ApiTimeCalculator.getFcstBaseDateTime();

		String urlBuilder = ULTRA_FCST_BASE_URL +
			"?serviceKey=" + apiKey +
			"&numOfRows=" + "1000" +// numOfRows(한 페이지 결과수)
			"&pageNo=" + "1" + // pageNo(페이지 번호)
			"&dataType=JSON" +
			"&base_date=" + baseTime.baseDate() +
			"&base_time=" + baseTime.baseTime() +
			"&nx=" + nx +
			"&ny=" + ny;

		return urlBuilder;
	}

	// 단기예보 getVilageFcst
	public String getVilageFcstUrl(int nx, int ny) {

		BaseDateTime baseTime = ApiTimeCalculator.getVilageFcstBaseDateTime();

		String urlBuilder = VILAGE_FCST_BASE_URL +
			"?serviceKey=" + apiKey +
			"&numOfRows=" + "1000" +// numOfRows(한 페이지 결과수)
			"&pageNo=" + "1" + // pageNo(페이지 번호)
			"&dataType=JSON" +
			"&base_date=" + baseTime.baseDate() +
			"&base_time=" + baseTime.baseTime() +
			"&nx=" + nx +
			"&ny=" + ny;

		return urlBuilder;
	}
}
