package com.sparta.spartatigers.domain.weather.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.team.repository.StadiumRepository;
import com.sparta.spartatigers.domain.weather.api.WeatherApiUrlGenerator;
import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.WeatherBundle;
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

	@Qualifier("weatherRestTemplate")
	private final RestTemplate restTemplate;
	private final WeatherApiUrlGenerator apiUrlGenerator;
	private final StadiumRepository stadiumRepository;

	/**
	 * NowCast + ForeCast를 단일 진입점에서 조회한다.
	 *
	 * Why: WeatherQueryService(구장날씨 탭)는 NowCast와 ForeCast를 함께 반환해야 한다.
	 * 기존에는 getNowCast / getForeCast를 각각 호출해 기상청 API를 최대 5회 직렬 호출했다.
	 * 이 메서드는 세 API(NCST / UltraFcst / VilageFcst)를 각 1회씩만 호출하고
	 * 결과를 NowCast · ForeCast 양쪽에 공유해 호출 횟수를 3회로 줄인다.
	 *
	 * @param stadiumId 구장 ID
	 * @return NowCast + ForeCast 묶음 (WeatherBundle)
	 */
	public WeatherBundle getNowCastAndForeCast(Long stadiumId) {

		Stadium stadium = stadiumRepository.findById(stadiumId)
				.orElseThrow(() -> new InvalidRequestException(ExceptionCode.STADIUM_NOT_FOUND));

		int nx = stadium.getNx();
		int ny = stadium.getNy();

		// ── 3개 API 호출 (직렬, 각 1회) ──────────────────────────────────────
		String ncstUrl = apiUrlGenerator.getUltraSrtNcstUrl(nx, ny);
		String ultraUrl = apiUrlGenerator.getUltraSrtFcstUrl(nx, ny);
		String vilageUrl = apiUrlGenerator.getVilageFcstUrl(nx, ny);

		OriginResponse ncstRes = restTemplate.getForObject(ncstUrl, OriginResponse.class);
		OriginResponse ultraRes = restTemplate.getForObject(ultraUrl, OriginResponse.class);
		OriginResponse vilageRes = restTemplate.getForObject(vilageUrl, OriginResponse.class);

		WeatherApiStatus ncstStatus = WeatherParser.classify(ncstRes);
		WeatherApiStatus ultraStatus = WeatherParser.classify(ultraRes);
		WeatherApiStatus vilageStatus = WeatherParser.classify(vilageRes);

		if (!ncstStatus.isSuccess() || !ultraStatus.isSuccess() || !vilageStatus.isSuccess()) {
			log.warn("Weather upstream status — ncst={}, ultra={}, vilage={} (stadiumId={})",
					ncstStatus, ultraStatus, vilageStatus, stadiumId);
		}

		// ── NowCast 조립 ──────────────────────────────────────────────────────
		List<OriginResponse.Item> ncstItems = WeatherParser.originItems(ncstRes);
		List<OriginResponse.Item> ultraItems = WeatherParser.originItems(ultraRes);

		Map<String, String> ncstMap = WeatherParser.toNcstMap(ncstItems);
		Map<String, String> ultraClosestMap = WeatherParser.toClosestFcstMap(ultraItems);

		Double rawTemp = WeatherParser.toNumberFromText(ncstMap.get("T1H"));
		Double rawRainAmt = WeatherParser.toNumberFromText(ncstMap.get("RN1"));
		Double rawWindSpeed = WeatherParser.toNumberFromText(ncstMap.get("WSD"));
		Double rawVec = WeatherParser.toNumberFromText(ncstMap.get("VEC"));

		// POP: 단기예보(vilage)의 현재 시각 슬롯에서 가져온다 (null 허용)
		List<OriginResponse.Item> vilageItems = WeatherParser.originItems(vilageRes);
		Map<String, String> vilageClosestMap = WeatherParser.toClosestFcstMap(vilageItems);
		Double rawPop = WeatherParser.toNumberFromText(vilageClosestMap.get("POP"));

		WindDirection windDirection = null;
		if (rawVec != null) {
			try {
				windDirection = WindDirection.fromDegree(rawVec);
			} catch (Exception e) {
				log.warn("WindDirection.fromDegree failed (vec={}, stadiumId={})", rawVec, stadiumId);
			}
		}

		NowCastResponseDto nowCast = NowCastResponseDto.of(
				LocalDateTime.now(),
				stadium,
				rawTemp,
				SkyStatus.fromCode(ultraClosestMap.get("SKY")),
				RainType.fromCode(ncstMap.get("PTY")),
				rawRainAmt,
				rawPop != null ? rawPop.intValue() : null,
				rawWindSpeed,
				windDirection);

		// ── ForeCast 조립 ─────────────────────────────────────────────────────
		Map<String, Map<String, Map<String, String>>> ultraMap = WeatherParser.toFcstMapGroupedByDateTime(ultraItems);
		Map<String, Map<String, Map<String, String>>> vilageMap = WeatherParser.toFcstMapGroupedByDateTime(vilageItems);

		List<ForeCastResponseDto> foreCastList = new ArrayList<>();

		for (Map.Entry<String, Map<String, Map<String, String>>> dateEntry : ultraMap.entrySet()) {
			String date = dateEntry.getKey();
			for (Map.Entry<String, Map<String, String>> timeEntry : dateEntry.getValue().entrySet()) {
				String time = timeEntry.getKey();
				Map<String, String> ultraFcst = timeEntry.getValue();

				Map<String, String> vilageFcst = vilageMap
						.getOrDefault(date, Collections.emptyMap())
						.getOrDefault(toHourAlignedKey(time), Collections.emptyMap());

				Double temp = WeatherParser.toNumberFromText(ultraFcst.get("T1H"));
				Double rainAmt = WeatherParser.toNumberFromText(ultraFcst.get("RN1"));
				Double popValue = WeatherParser.toNumberFromText(vilageFcst.get("POP"));

				foreCastList.add(ForeCastResponseDto.of(
						WeatherParser.toDateTimeFromFcst(date, time),
						stadium,
						temp,
						SkyStatus.fromCode(ultraFcst.get("SKY")),
						popValue != null ? popValue.intValue() : 0,
						RainType.fromCode(ultraFcst.get("PTY")),
						rainAmt));
			}
		}

		return new WeatherBundle(nowCast, foreCastList);
	}

	// ── 기존 단독 조회 메서드 (WeatherController 등 기존 호출부 호환 유지) ──────

	public NowCastResponseDto getNowCast(Long stadiumId) {
		return getNowCastAndForeCast(stadiumId).nowCast();
	}

	public List<ForeCastResponseDto> getForeCast(Long stadiumId) {
		return getNowCastAndForeCast(stadiumId).foreCast();
	}

	// ── 내부 유틸 ─────────────────────────────────────────────────────────────

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
}
