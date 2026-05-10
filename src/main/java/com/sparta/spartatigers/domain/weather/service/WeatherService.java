package com.sparta.spartatigers.domain.weather.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.team.repository.StadiumRepository;
import com.sparta.spartatigers.domain.weather.api.WeatherApiUrlGenerator;
import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;
import com.sparta.spartatigers.domain.weather.model.WindDirection;
import com.sparta.spartatigers.domain.weather.response.OriginResponse;
import com.sparta.spartatigers.domain.weather.util.WeatherParser;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final WeatherApiUrlGenerator apiUrlGenerator;
	private final StadiumRepository stadiumRepository;

	public NowCastResponseDto getNowCast(Long stadiumId) {

		Stadium stadium = stadiumRepository.findById(stadiumId)
				.orElseThrow(() -> new InvalidRequestException(ExceptionCode.STADIUM_NOT_FOUND));
		int nx = stadium.getNx();
		int ny = stadium.getNy();

		String ncstUrl = apiUrlGenerator.getUltraSrtNcstUrl(nx, ny);
		String fcstUrl = apiUrlGenerator.getUltraSrtFcstUrl(nx, ny);

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

		// 강수확률(POP)은 단기예보에서만 제공 → 실패해도 null fallback
		Integer rainProbability = fetchClosestPop(nx, ny);

		return NowCastResponseDto.of(
				LocalDateTime.now(),
				stadium,
				temperature,
				skyStatus,
				rainType,
				rainAmount,
				rainProbability,
				windSpeed,
				windDirection);

	}

	public List<ForeCastResponseDto> getForeCast(Long stadiumId) {

		Stadium stadium = stadiumRepository.findById(stadiumId)
				.orElseThrow(() -> new InvalidRequestException(ExceptionCode.STADIUM_NOT_FOUND));

		int nx = stadium.getNx();
		int ny = stadium.getNy();

		String ultraNcstUrl = apiUrlGenerator.getUltraSrtFcstUrl(nx, ny);
		String vilageFcstUrl = apiUrlGenerator.getVilageFcstUrl(nx, ny);

		OriginResponse ultraRes = restTemplate.getForObject(ultraNcstUrl, OriginResponse.class);
		OriginResponse vilageRes = restTemplate.getForObject(vilageFcstUrl, OriginResponse.class);

		List<OriginResponse.Item> ultraItems = WeatherParser.originItems(ultraRes);
		List<OriginResponse.Item> vilageItems = WeatherParser
				.normalizeVilageTimes(WeatherParser.originItems(vilageRes));

		Map<String, Map<String, String>> ultraMap = WeatherParser.toFcstMapGroupedByTime(ultraItems);
		Map<String, Map<String, String>> vilageMap = WeatherParser.toFcstMapGroupedByTime(vilageItems);

		List<ForeCastResponseDto> foreCastList = new ArrayList<>();

		for (String time : ultraMap.keySet()) {
			Map<String, String> ultraFcst = ultraMap.get(time);
			String vKey = time.endsWith("00") ? time.substring(0, 2) + "30" : time;
			Map<String, String> vilageFcst = vilageMap.getOrDefault(vKey, Collections.emptyMap());

			Double temp = WeatherParser.toNumberFromText(ultraFcst.get("T1H"));
			Double rainAmount = WeatherParser.toNumberFromText(ultraFcst.get("RN1"));
			Double popValue = WeatherParser.toNumberFromText(vilageFcst.get("POP"));

			double temperature = temp != null ? temp : Double.NaN;
			double rain = rainAmount != null ? rainAmount : 0.0;
			int pop = popValue != null ? popValue.intValue() : 0;

			foreCastList.add(ForeCastResponseDto.of(
					WeatherParser.toDateTimeFromFcst(time),
					stadium,
					temperature,
					SkyStatus.fromCode(ultraFcst.get("SKY")),
					pop,
					RainType.fromCode(ultraFcst.get("PTY")),
					rain));
		}

		return foreCastList;
	}

	/**
	 * 단기예보(getVilageFcst)로부터 현재 시각에 가장 가까운 시각의 POP(강수확률)를 조회.
	 * 호출 실패/응답 결손/값 부재 시 null을 반환하여 NowCast 전체가 실패하지 않도록 격리한다.
	 */
	private Integer fetchClosestPop(int nx, int ny) {
		try {
			String vilageUrl = apiUrlGenerator.getVilageFcstUrl(nx, ny);
			OriginResponse vilageRes = restTemplate.getForObject(vilageUrl, OriginResponse.class);

			List<OriginResponse.Item> vilageItems = WeatherParser
					.normalizeVilageTimes(WeatherParser.originItems(vilageRes));

			Map<String, String> vilageClosest = WeatherParser.toClosestFcstMap(vilageItems);
			Double popValue = WeatherParser.toNumberFromText(vilageClosest.get("POP"));

			return popValue != null ? popValue.intValue() : null;
		} catch (Exception e) {
			log.warn("Vilage fcst call failed while fetching POP (nx={}, ny={}): {}", nx, ny, e.getMessage());
			return null;
		}
	}
}
