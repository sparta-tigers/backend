package com.sparta.spartatigers.domain.weather.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sparta.spartatigers.domain.liveboard.model.Stadium;
import com.sparta.spartatigers.domain.liveboard.repository.StadiumRepository;
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
	private final Clock clock;

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

		// ── 3개 API URL 생성 ──────────────────────────────────────────────────
		String ncstUrl = apiUrlGenerator.getUltraSrtNcstUrl(nx, ny);
		String ultraUrl = apiUrlGenerator.getUltraSrtFcstUrl(nx, ny);
		String vilageUrl = apiUrlGenerator.getVilageFcstUrl(nx, ny);

		// ── 3개 API 병렬 호출 (비동기, 타임아웃 6초) ──────────────────────────
		CompletableFuture<OriginResponse> ncstFuture = CompletableFuture.supplyAsync(
				() -> restTemplate.getForObject(ncstUrl, OriginResponse.class));
		CompletableFuture<OriginResponse> ultraFuture = CompletableFuture.supplyAsync(
				() -> restTemplate.getForObject(ultraUrl, OriginResponse.class));
		CompletableFuture<OriginResponse> vilageFuture = CompletableFuture.supplyAsync(
				() -> restTemplate.getForObject(vilageUrl, OriginResponse.class));

		OriginResponse ncstRes;
		OriginResponse ultraRes;
		OriginResponse vilageRes;

		try {
			// 3개 중 하나라도 6초 이상 걸리면 타임아웃 (전체 지연 방지)
			CompletableFuture.allOf(ncstFuture, ultraFuture, vilageFuture)
					.get(6, TimeUnit.SECONDS);

			ncstRes = ncstFuture.join();
			ultraRes = ultraFuture.join();
			vilageRes = vilageFuture.join();

		} catch (Exception e) {
			log.error("Weather API call failed or timed out (stadiumId={}): {}", stadiumId, e.getMessage());
			// 장애 전이 방지를 위해 즉시 UPSTREAM_ERROR 상태로 반환 (Graceful Degradation)
			return new WeatherBundle(WeatherApiStatus.UPSTREAM_ERROR,
					NowCastResponseDto.empty(stadium, LocalDateTime.now(clock)),
					Collections.emptyList());
		}

		WeatherApiStatus ncstStatus = WeatherParser.classify(ncstRes);
		WeatherApiStatus ultraStatus = WeatherParser.classify(ultraRes);
		WeatherApiStatus vilageStatus = WeatherParser.classify(vilageRes);

		if (!ncstStatus.isSuccess() || !ultraStatus.isSuccess() || !vilageStatus.isSuccess()) {
			log.warn("Weather upstream status — ncst={}, ultra={}, vilage={} (stadiumId={})",
					ncstStatus, ultraStatus, vilageStatus, stadiumId);
		}

		// 세 API 중 가장 심각한 상태를 대표값으로 선택
		WeatherApiStatus overallStatus = worstStatus(ncstStatus, ultraStatus, vilageStatus);

		// ── NowCast 조립 ──────────────────────────────────────────────────────
		List<OriginResponse.Item> ncstItems = WeatherParser.originItems(ncstRes);
		List<OriginResponse.Item> ultraItems = WeatherParser.originItems(ultraRes);

		Map<String, String> ncstMap = WeatherParser.toNcstMap(ncstItems);
		Map<String, String> ultraClosestMap = WeatherParser.toClosestFcstMap(ultraItems, clock);

		Double rawTemp = WeatherParser.toNumberFromText(ncstMap.get("T1H"));
		Double rawRainAmt = WeatherParser.toNumberFromText(ncstMap.get("RN1"));
		Double rawWindSpeed = WeatherParser.toNumberFromText(ncstMap.get("WSD"));
		Double rawVec = WeatherParser.toNumberFromText(ncstMap.get("VEC"));

		// POP: 단기예보(vilage)의 현재 시각 슬롯에서 가져온다 (null 허용)
		List<OriginResponse.Item> vilageItems = WeatherParser.originItems(vilageRes);
		Map<String, String> vilageClosestMap = WeatherParser.toClosestFcstMap(vilageItems, clock);
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
				LocalDateTime.now(clock),
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
						popValue != null ? popValue.intValue() : null,
						RainType.fromCode(ultraFcst.get("PTY")),
						rainAmt));
			}
		}

		return new WeatherBundle(overallStatus, nowCast, foreCastList);
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

	/**
	 * 세 API 상태 중 가장 심각한 상태를 반환한다.
	 * 우선순위: INTERNAL_ERROR > UPSTREAM_ERROR > NO_DATA > SUCCESS
	 */
	private static WeatherApiStatus worstStatus(WeatherApiStatus... statuses) {
		WeatherApiStatus worst = WeatherApiStatus.SUCCESS;
		for (WeatherApiStatus s : statuses) {
			if (s == WeatherApiStatus.INTERNAL_ERROR)
				return WeatherApiStatus.INTERNAL_ERROR;
			if (s == WeatherApiStatus.UPSTREAM_ERROR)
				worst = WeatherApiStatus.UPSTREAM_ERROR;
			else if (s == WeatherApiStatus.NO_DATA && worst == WeatherApiStatus.SUCCESS)
				worst = WeatherApiStatus.NO_DATA;
		}
		return worst;
	}
}
