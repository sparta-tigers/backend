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
import com.sparta.spartatigers.domain.weather.model.WeatherApiStatus;
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

		WeatherApiStatus ncstStatus = WeatherParser.classify(ncstRes);
		WeatherApiStatus fcstStatus = WeatherParser.classify(fcstRes);
		if (!ncstStatus.isSuccess() || !fcstStatus.isSuccess()) {
			log.warn("NowCast upstream status — ncst={}, fcst={} (stadiumId={})",
					ncstStatus, fcstStatus, stadiumId);
		}

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

		WeatherApiStatus ultraStatus = WeatherParser.classify(ultraRes);
		WeatherApiStatus vilageStatus = WeatherParser.classify(vilageRes);
		if (!ultraStatus.isSuccess() || !vilageStatus.isSuccess()) {
			log.warn("ForeCast upstream status — ultra={}, vilage={} (stadiumId={})",
					ultraStatus, vilageStatus, stadiumId);
		}

		List<OriginResponse.Item> ultraItems = WeatherParser.originItems(ultraRes);
		List<OriginResponse.Item> vilageItems = WeatherParser.originItems(vilageRes);

		// (fcstDate → (fcstTime → (category → value))) 구조로 조합
		// Why: 초단기예보는 자정 이후 시간을 포함할 수 있어 fcstTime만으로 키를 만들면
		// 오늘 HHmm과 다음날 HHmm이 충돌한다. fcstDate를 키에 포함해 방어한다.
		Map<String, Map<String, Map<String, String>>> ultraMap = WeatherParser.toFcstMapGroupedByDateTime(ultraItems);
		Map<String, Map<String, Map<String, String>>> vilageMap = WeatherParser.toFcstMapGroupedByDateTime(vilageItems);

		List<ForeCastResponseDto> foreCastList = new ArrayList<>();

		for (Map.Entry<String, Map<String, Map<String, String>>> dateEntry : ultraMap.entrySet()) {
			String date = dateEntry.getKey();
			for (Map.Entry<String, Map<String, String>> timeEntry : dateEntry.getValue().entrySet()) {
				String time = timeEntry.getKey();
				Map<String, String> ultraFcst = timeEntry.getValue();

				// 단기예보는 정각(HH00)만 제공되므로 초단기 HH30 데이터와 짝이 없다.
				// 같은 날짜의 HH00 슬롯에서 POP를 가져온다.
				Map<String, String> vilageFcst = vilageMap
						.getOrDefault(date, Collections.emptyMap())
						.getOrDefault(toHourAlignedKey(time), Collections.emptyMap());

				Double temp = WeatherParser.toNumberFromText(ultraFcst.get("T1H"));
				Double rainAmount = WeatherParser.toNumberFromText(ultraFcst.get("RN1"));
				Double popValue = WeatherParser.toNumberFromText(vilageFcst.get("POP"));

				double temperature = temp != null ? temp : Double.NaN;
				double rain = rainAmount != null ? rainAmount : 0.0;
				int pop = popValue != null ? popValue.intValue() : 0;

				foreCastList.add(ForeCastResponseDto.of(
						WeatherParser.toDateTimeFromFcst(date, time),
						stadium,
						temperature,
						SkyStatus.fromCode(ultraFcst.get("SKY")),
						pop,
						RainType.fromCode(ultraFcst.get("PTY")),
						rain));
			}
		}

		return foreCastList;
	}

	/**
	 * HH30 → HH00 정렬된 key로 변환.
	 *
	 * Why: 초단기예보는 HH30 단위, 단기예보는 HH00 단위로 제공된다.
	 * 초단기 HH30에 매칭되는 단기 POP은 동일 시각의 HH00 슬롯에서 가져와야
	 * "다음 정시까지의 강수확률"이 일관되게 표시된다.
	 */
	private static String toHourAlignedKey(String hhmm) {
		if (hhmm == null || hhmm.length() != 4)
			return hhmm;
		return hhmm.substring(0, 2) + "00";
	}

	/**
	 * 단기예보(getVilageFcst)로부터 현재 시각에 가장 가까운 시각의 POP(강수확률)를 조회.
	 * 호출 실패/응답 결손/값 부재 시 null을 반환하여 NowCast 전체가 실패하지 않도록 격리한다.
	 */
	private Integer fetchClosestPop(int nx, int ny) {
		try {
			String vilageUrl = apiUrlGenerator.getVilageFcstUrl(nx, ny);
			OriginResponse vilageRes = restTemplate.getForObject(vilageUrl, OriginResponse.class);

			WeatherApiStatus status = WeatherParser.classify(vilageRes);
			if (!status.isSuccess()) {
				log.warn("Vilage fcst status {} while fetching POP (nx={}, ny={})", status, nx, ny);
				return null;
			}

			List<OriginResponse.Item> vilageItems = WeatherParser.originItems(vilageRes);

			Map<String, String> vilageClosest = WeatherParser.toClosestFcstMap(vilageItems);
			Double popValue = WeatherParser.toNumberFromText(vilageClosest.get("POP"));

			return popValue != null ? popValue.intValue() : null;
		} catch (Exception e) {
			log.warn("Vilage fcst call failed while fetching POP (nx={}, ny={}): {}", nx, ny, e.getMessage());
			return null;
		}
	}
}
