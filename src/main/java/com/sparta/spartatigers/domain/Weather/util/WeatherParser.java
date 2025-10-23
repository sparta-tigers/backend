package com.sparta.spartatigers.domain.weather.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.model.RainType;
import com.sparta.spartatigers.domain.weather.model.SkyStatus;
import com.sparta.spartatigers.domain.weather.model.WindDirection;
import com.sparta.spartatigers.domain.weather.response.OriginResponse;

public class WeatherParser {

	// 원본 응답 속 일부 필드 비어있는 경우 무시
	public static List<OriginResponse.Item> originItems(OriginResponse res) {
		if(res == null || res.response == null || res.response.body == null || res.response.body.items == null || res.response.body.items.item == null ) {
			return Collections.emptyList();
		}
		return res.response.body.items.item;
	}

	// 초단기실황 : 카테고리 - 응답 매핑
	public static Map<String, String> toNcstMap(List<OriginResponse.Item> items) {
		Map<String, String> m = new HashMap<>();
		for (OriginResponse.Item it : items) {
			if(it.category != null && it.obsrValue != null) { // obsrValue는 실황용 응답값임
				m.put(it.category, it.obsrValue);
			}
		} return m;
	}

	// 초단기예보 : 카테고리 - 응답 매핑 (현재시간만)
	public static Map<String, String> toFcstMap(List<OriginResponse.Item> items) {
		Map<String, String> m = new HashMap<>();
		for (OriginResponse.Item it : items) {
			if(it.category != null && it.fcstValue != null) { // fcstValue는 실황용 응답값임
				m.put(it.category, it.fcstValue);
			}
		} return m;
	}

	// 초단기예보 , 단기예보 : 시간별 예보값 전체 추출
	public static Map<String, Map<String, String>> toFcstMapGroupedByTime(List<OriginResponse.Item> items) {
		Map<String, Map<String, String>> timeCategoryMap = new LinkedHashMap<>();

		for (OriginResponse.Item item : items) {
			if (item.fcstTime == null || item.category == null || item.fcstValue == null) continue;

			timeCategoryMap
				.computeIfAbsent(item.fcstTime, t -> new HashMap<>())
				.put(item.category, item.fcstValue);
		}

		return timeCategoryMap;
	}

	// 가장 최근 발표 찾기
	public static LocalDateTime latestBaseDateTime (List<OriginResponse.Item> items) {
		LocalDateTime latest = null;
		for (OriginResponse.Item it : items) {
			if (it.baseTime==null || it.baseDate==null) continue;
			LocalDateTime target = toDateTime(it.baseDate,it.baseTime);
			if(target != null && (latest == null || target.isAfter(latest)))
				latest = target;
		}
		return latest;
	}

	// 구장의 위경도 찾기
	public static Integer extractNx(List<OriginResponse.Item> itemLists) {
		for (OriginResponse.Item item : itemLists) {
			if(item.nx != null) return item.nx;
		} return null;
	}

	public static Integer extractNy(List<OriginResponse.Item> itemLists) {
		for (OriginResponse.Item item : itemLists) {
			if(item.ny != null) return item.ny;
		} return null;
	}

	// 응답속 String -> LocalDateTime으로 변환
	public static LocalDateTime toDateTime(String yyyymmdd, String hhmm) {
		try {
			LocalDate date = LocalDate.of(
				Integer.parseInt(yyyymmdd.substring(0,4)),
				Integer.parseInt(yyyymmdd.substring(4,6)),
				Integer.parseInt(yyyymmdd.substring(6,8)));
			LocalTime time = LocalTime.of(
				Integer.parseInt(hhmm.substring(0,2)),
				Integer.parseInt(hhmm.substring(2,4)));
			return LocalDateTime.of(date, time);
		} catch (Exception e) {
			return null;
		}
	}

	// 원본 응답속 m/s , mm 등등 문자열 빼고 double로!
	public static Double toNumberFromText(String s) {
		if (s == null) return null;
		String cleaned = s.replaceAll("[^0-9+\\-.]", "");
		if (cleaned.isEmpty() || cleaned.equals("-")) return null;
		try {
			return Double.parseDouble(cleaned);
		} catch (Exception e) {
			return null;
		}
	}


	// ------------------------------------------------


	// 현재 날씨 생성 (초단기실황 + 초단기예보)
	public static NowCastResponseDto toNowCast(
		OriginResponse ultraNcst,
		OriginResponse ultraFcst,
		String targetDate,
		String targetTime
	) {
		List<OriginResponse.Item> ncstItems = originItems(ultraNcst);
		List<OriginResponse.Item> fcstItems = originItems(ultraFcst);

		Map<String, String> ncstMap = toNcstMap(ncstItems);
		Map<String, String> fcstMap = toFcstMap(fcstItems, targetDate, targetTime);

		LocalDateTime referenceTime = LocalDateTime.now();
		Integer nx = extractNx(ncstItems);
		Integer ny = extractNy(ncstItems);

		double temperature = toNumberFromText(ncstMap.get("T1H"));
		SkyStatus skyStatus = SkyStatus.fromCode(fcstMap.get("SKY"));
		RainType rainType = RainType.fromCode(ncstMap.get("PTY"));
		double rainAmount = toNumberFromText(ncstMap.get("RN1"));
		double windSpeed = toNumberFromText(ncstMap.get("WSD"));
		WindDirection windDirection = WindDirection.fromDegree(toNumberFromText(ncstMap.get("VEC")));

		return NowCastResponseDto.of(
			referenceTime,
			null,
			temperature,
			skyStatus,
			rainType,
			rainAmount,
			windSpeed,
			windDirection
		);

	}

}

