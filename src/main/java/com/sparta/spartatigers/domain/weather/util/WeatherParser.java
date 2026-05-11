package com.sparta.spartatigers.domain.weather.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sparta.spartatigers.domain.weather.model.WeatherApiStatus;
import com.sparta.spartatigers.domain.weather.response.OriginResponse;

public class WeatherParser {

	/**
	 * 기상청 응답을 상위 상태로 분류한다.
	 *
	 * Why: 호출자가 header.resultCode를 매번 판독할 필요 없이 네 가지 상태로 추상화해
	 * "점검 중", "데이터 없음", "정상" 분기를 명확히 표현하기 위함.
	 */
	public static WeatherApiStatus classify(OriginResponse res) {
		if (res == null || res.response == null) {
			return WeatherApiStatus.INTERNAL_ERROR;
		}
		OriginResponse.Header header = res.response.header;
		if (header == null || header.resultCode == null) {
			return WeatherApiStatus.INTERNAL_ERROR;
		}

		String code = header.resultCode;
		if ("00".equals(code)) {
			boolean hasItems = res.response.body != null
					&& res.response.body.items != null
					&& res.response.body.items.item != null
					&& !res.response.body.items.item.isEmpty();
			return hasItems ? WeatherApiStatus.SUCCESS : WeatherApiStatus.NO_DATA;
		}
		if ("03".equals(code)) {
			return WeatherApiStatus.NO_DATA;
		}
		// 01, 02, 04, 05 및 그 외 기상청 쪽 에러
		return WeatherApiStatus.UPSTREAM_ERROR;
	}

	// 원본 응답 속 일부 필드 비어있는 경우 무시
	public static List<OriginResponse.Item> originItems(OriginResponse res) {
		if (res == null || res.response == null || res.response.body == null || res.response.body.items == null
				|| res.response.body.items.item == null) {
			return Collections.emptyList();
		}
		return res.response.body.items.item;
	}

	// ✅ 초단기실황 : 카테고리 - 응답 매핑
	public static Map<String, String> toNcstMap(List<OriginResponse.Item> items) {
		Map<String, String> m = new HashMap<>();
		for (OriginResponse.Item it : items) {
			if (it.category != null && it.obsrValue != null) { // obsrValue는 실황용 응답값임
				m.put(it.category, it.obsrValue);
			}
		}
		return m;
	}

	// ✅ 초단기예보 : 카테고리 - 응답 매핑 (현재시간만)
	public static Map<String, String> toClosestFcstMap(List<OriginResponse.Item> items) {

		LocalDateTime closest = getClosestTimeToNow(items);
		if (closest == null)
			return Collections.emptyMap();
		String targetDate = closest.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String targetTime = closest.format(DateTimeFormatter.ofPattern("HHmm"));

		Map<String, String> m = new HashMap<>();
		for (OriginResponse.Item it : items) {
			if (targetDate.equals(it.fcstDate) && targetTime.equals(it.fcstTime)) {
				if (it.category != null && it.fcstValue != null) { // fcstValue는 실황용 응답값임
					m.put(it.category, it.fcstValue);
				}
			}
		}
		return m;
	}

	/**
	 * 초단기예보/단기예보 응답을 (fcstDate+fcstTime) 단위로 그룹핑한다.
	 *
	 * Why: 초단기예보는 최대 6시간 뒤까지의 예보를 한 번에 주므로 자정을 넘는 예보가 포함된다.
	 * 기존 구현은 fcstTime만으로 그룹핑해 다음날 00시와 오늘 00시가 동일 키로 충돌하거나,
	 * 상위 {@link #toDateTimeFromFcst(String)}가 오늘 날짜로만 매핑해 자정 이후 예보가
	 * 과거로 밀리는 문제가 있었다. fcstDate 정보를 키에 포함해 이를 차단한다.
	 *
	 * @return fcstDate(yyyyMMdd) → (fcstTime(HHmm) → (category → fcstValue))
	 */
	public static Map<String, Map<String, Map<String, String>>> toFcstMapGroupedByDateTime(
			List<OriginResponse.Item> items) {
		Map<String, Map<String, Map<String, String>>> dateTimeCategoryMap = new LinkedHashMap<>();

		for (OriginResponse.Item item : items) {
			if (item.fcstDate == null || item.fcstTime == null
					|| item.category == null || item.fcstValue == null)
				continue;

			dateTimeCategoryMap
					.computeIfAbsent(item.fcstDate, d -> new LinkedHashMap<>())
					.computeIfAbsent(item.fcstTime, t -> new HashMap<>())
					.put(item.category, item.fcstValue);
		}

		return dateTimeCategoryMap;
	}

	/**
	 * 예보 항목 중 지금 이후 가장 가까운 시각을 찾는다.
	 *
	 * Why: NowCast에서 "현재"의 SKY/PTY를 예보로 채울 때, 절댓값 기반으로 이미 지난
	 * 시각을 고르면 한 시간 전 상태가 표시될 수 있었다. 현재 정시 이상의 예보만
	 * 후보로 삼고 가장 가까운 것을 선택해 "지금"의 의미를 맞춘다.
	 *
	 * 경계값: 정시 경계 직전에 호출될 경우 "지금 이후 예보"가 아예 없는 경우를 피하기 위해
	 * 현재 시각을 시 단위로 내림한 "정시 기준" 이상을 허용한다.
	 */
	public static LocalDateTime getClosestTimeToNow(List<OriginResponse.Item> items) {
		LocalDateTime threshold = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
		LocalDateTime closest = null;

		for (OriginResponse.Item it : items) {
			if (it.fcstDate == null || it.fcstTime == null)
				continue;

			LocalDateTime fcstDateTime = toDateTime(it.fcstDate, it.fcstTime);
			if (fcstDateTime == null)
				continue;
			if (fcstDateTime.isBefore(threshold))
				continue;

			if (closest == null || fcstDateTime.isBefore(closest)) {
				closest = fcstDateTime;
			}
		}

		return closest;
	}

	// 가장 최근 발표 찾기
	public static LocalDateTime latestBaseDateTime(List<OriginResponse.Item> items) {
		LocalDateTime latest = null;
		for (OriginResponse.Item it : items) {
			if (it.baseTime == null || it.baseDate == null)
				continue;
			LocalDateTime target = toDateTime(it.baseDate, it.baseTime);
			if (target != null && (latest == null || target.isAfter(latest)))
				latest = target;
		}
		return latest;
	}

	// 응답속 String -> LocalDateTime으로 변환
	public static LocalDateTime toDateTime(String yyyymmdd, String hhmm) {
		try {
			LocalDate date = LocalDate.of(
					Integer.parseInt(yyyymmdd.substring(0, 4)),
					Integer.parseInt(yyyymmdd.substring(4, 6)),
					Integer.parseInt(yyyymmdd.substring(6, 8)));
			LocalTime time = LocalTime.of(
					Integer.parseInt(hhmm.substring(0, 2)),
					Integer.parseInt(hhmm.substring(2, 4)));
			return LocalDateTime.of(date, time);
		} catch (Exception e) {
			return null;
		}
	}

	// 원본 응답속 m/s , mm 등등 문자열 빼고 double로!
	public static Double toNumberFromText(String s) {
		if (s == null)
			return null;
		String cleaned = s.replaceAll("[^0-9+\\-.]", "");
		if (cleaned.isEmpty() || cleaned.equals("-"))
			return null;
		try {
			return Double.parseDouble(cleaned);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 예보 응답의 fcstDate/fcstTime을 LocalDateTime으로 변환한다.
	 *
	 * Why: 기존에는 fcstTime만 받고 날짜를 LocalDate.now()로 고정해, 초단기예보에 포함된
	 * 자정 이후 예보가 "오늘 같은 시각"으로 잘못 매핑되어 프론트 필터에서 과거로 밀렸다.
	 */
	public static LocalDateTime toDateTimeFromFcst(String fcstDate, String fcstTime) {
		return toDateTime(fcstDate, fcstTime);
	}

}
