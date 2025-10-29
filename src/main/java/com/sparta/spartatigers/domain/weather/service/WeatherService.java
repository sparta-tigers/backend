package com.sparta.spartatigers.domain.weather.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.weather.api.WeatherApiUrlGenerator;
import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;
import com.sparta.spartatigers.domain.weather.model.WindDirection;
import com.sparta.spartatigers.domain.weather.response.OriginResponse;
import com.sparta.spartatigers.domain.weather.util.WeatherParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeatherService {

	private final RestTemplate restTemplate = new RestTemplate();

	public NowCastResponseDto getNowCast(Stadium stadium) {

		int nx = stadium.getNx();
		int ny = stadium.getNy();

		String ncstUrl = WeatherApiUrlGenerator.getUltraSrtNcstUrl(nx, ny);
		String fcstUrl = WeatherApiUrlGenerator.getVilageFcstUrl(nx, ny);

		OriginResponse ncstRes = restTemplate.getForObject(ncstUrl, OriginResponse.class);
		OriginResponse fcstRes = restTemplate.getForObject(fcstUrl, OriginResponse.class);

		List<OriginResponse.Item> ncstItems = WeatherParser.originItems(ncstRes);
		List<OriginResponse.Item> fcstItems = WeatherParser.originItems(fcstRes);

		Map<String, String> ncstMap = WeatherParser.toNcstMap(ncstItems);
		Map<String, String> fcstMap = WeatherParser.toClosestFcstMap(fcstItems);

		double temperature = WeatherParser.toNumberFromText(ncstMap.get("T1H"));
		SkyStatus skyStatus = SkyStatus.fromCode(fcstMap.get("SKY"));
		RainType rainType = RainType.fromCode(ncstMap.get("PTY"));
		double rainAmount = WeatherParser.toNumberFromText(ncstMap.get("RN1"));
		double windSpeed = WeatherParser.toNumberFromText(ncstMap.get("WSD"));
		WindDirection windDirection = WindDirection.fromDegree(WeatherParser.toNumberFromText(ncstMap.get("VEC")));

		return NowCastResponseDto.of(
			LocalDateTime.now(),
			stadium,
			temperature,
			skyStatus,
			rainType,
			rainAmount,
			windSpeed,
			windDirection
		);

	}

	public List<ForeCastResponseDto> getForeCast(Stadium stadium) {

		int nx = stadium.getNx();
		int ny = stadium.getNy();

		String ultraNcstUrl = WeatherApiUrlGenerator.getUltraSrtFcstUrl(nx, ny);
		String vilageFcstUrl = WeatherApiUrlGenerator.getVilageFcstUrl(nx, ny);

		OriginResponse ultraRes = restTemplate.getForObject(ultraNcstUrl, OriginResponse.class);
		OriginResponse vilageRes = restTemplate.getForObject(vilageFcstUrl, OriginResponse.class);

		List<OriginResponse.Item> ultraItems = WeatherParser.originItems(ultraRes);
		List<OriginResponse.Item> vilageItems = WeatherParser.normalizeVilageTimes(WeatherParser.originItems(vilageRes));

		Map<String, Map<String, String>> ultraMap = WeatherParser.toFcstMapGroupedByTime(ultraItems);
		Map<String, Map<String, String>> vilageMap = WeatherParser.toFcstMapGroupedByTime(vilageItems);

		List<ForeCastResponseDto> foreCastList = new ArrayList<>();

		for (String time : ultraMap.keySet()) {
			Map<String, String> ultraFcst = ultraMap.get(time);

			// 1) 초단기 키도 HH00이면 HH30으로 보정해서 단기맵 접근
			String vKey = time.endsWith("00") ? time.substring(0, 2) + "30" : time;

			// 2) NPE 방어: 키가 없어도 빈 맵
			Map<String, String> vilageFcst = vilageMap.getOrDefault(vKey, java.util.Collections.emptyMap());

			Double temp       = WeatherParser.toNumberFromText(ultraFcst.get("T1H"));
			Double rainAmount = WeatherParser.toNumberFromText(ultraFcst.get("RN1"));
			Double popValue   = WeatherParser.toNumberFromText(vilageFcst.get("POP"));

			double temperature = temp != null ? temp : Double.NaN;
			double rain        = rainAmount != null ? rainAmount : 0.0;
			int    pop         = popValue != null ? popValue.intValue() : 0;

			foreCastList.add(ForeCastResponseDto.of(
				WeatherParser.toDateTimeFromFcst(time),
				stadium,
				temperature,
				SkyStatus.fromCode(ultraFcst.get("SKY")),
				pop,
				RainType.fromCode(ultraFcst.get("PTY")),
				rain
			));
		}

		// for (String time : ultraMap.keySet()) {
		// 	Map<String, String> ultraFcst = ultraMap.get(time);
		// 	Map<String, String> vilageFcst = vilageMap.get(time);
		//
		// 	Double temp = WeatherParser.toNumberFromText(ultraFcst.get("T1H"));
		// 	Double rainAmount = WeatherParser.toNumberFromText(ultraFcst.get("RN1"));
		// 	Double popValue = WeatherParser.toNumberFromText(vilageFcst.get("POP"));
		//
		// 	// ✅ null-safe 기본값 처리
		// 	double temperature = temp != null ? temp : Double.NaN;
		// 	double rain = rainAmount != null ? rainAmount : 0.0;
		// 	int pop = popValue != null ? popValue.intValue() : 0;
		//
		// 	ForeCastResponseDto dto = ForeCastResponseDto.of(
		// 		WeatherParser.toDateTimeFromFcst(time),
		// 		stadium,
		// 		temperature,
		// 		SkyStatus.fromCode(ultraFcst.get("SKY")),
		// 		pop,
		// 		RainType.fromCode(ultraFcst.get("PTY")),
		// 		rain
		// 	);
		// 	foreCastList.add(dto);
		// }
		return foreCastList;
	}



}
