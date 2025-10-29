package com.sparta.spartatigers.domain.weather.api;
import com.sparta.spartatigers.domain.weather.api.ApiTimeCalculator.BaseDateTime;

import java.io.UnsupportedEncodingException;

public class WeatherApiUrlGenerator {

	private static final String API_KEY = "eeaf6b6e2f691626df4fd5679bdd481e4860401825e664dadeacacf3160dd28c";
	private static final String ULTRA_NCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
	private static final String ULTRA_FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
	private static final String VILAGE_FCST_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

	// 초단기실황 getUltraSrtNcst
	public static String getUltraSrtNcstUrl(int nx, int ny) {

		BaseDateTime baseTime = ApiTimeCalculator.getNcstBaseDateTime();

		String urlBuilder = ULTRA_NCST_BASE_URL +
			"?serviceKey=" + API_KEY +
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
	public static String getUltraSrtFcstUrl(int nx, int ny)  {

		BaseDateTime baseTime = ApiTimeCalculator.getFcstBaseDateTime();

		String urlBuilder = ULTRA_FCST_BASE_URL +
			"?serviceKey=" + API_KEY +
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
	public static String getVilageFcstUrl(int nx, int ny) {

		BaseDateTime baseTime = ApiTimeCalculator.getVilageFcstBaseDateTime();

		String urlBuilder = VILAGE_FCST_BASE_URL +
			"?serviceKey=" + API_KEY +
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
