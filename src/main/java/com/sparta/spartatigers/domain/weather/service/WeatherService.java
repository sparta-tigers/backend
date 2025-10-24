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
		List<OriginResponse.Item> vilageItems = WeatherParser.originItems(vilageRes);

		Map<String, Map<String, String>> ultraMap = WeatherParser.toFcstMapGroupedByTime(ultraItems);
		Map<String, Map<String, String>> vilageMap = WeatherParser.toFcstMapGroupedByTime(vilageItems);

		List<ForeCastResponseDto> foreCastList = new ArrayList<>();

		for (String time : ultraMap.keySet()) {
			Map<String, String> ultraFcst = ultraMap.get(time);
			Map<String, String> vilageFcst = vilageMap.get(time);

			ForeCastResponseDto dto = ForeCastResponseDto.of(
				WeatherParser.toDateTimeFromFcst(time),
				stadium,
				WeatherParser.toNumberFromText(ultraFcst.get("T1H")),
				SkyStatus.fromCode(ultraFcst.get("SKY")),
				WeatherParser.toNumberFromText(vilageFcst.get("POP")).intValue(),
				RainType.fromCode(ultraFcst.get("PTY")),
				WeatherParser.toNumberFromText(ultraFcst.get("RN1"))
			);
			foreCastList.add(dto);
		}
		return foreCastList;
	}



}
