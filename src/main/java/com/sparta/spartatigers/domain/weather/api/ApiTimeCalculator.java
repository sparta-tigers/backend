package com.sparta.spartatigers.domain.weather.api;

import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 기상청 발표 시각 계산기
 *
 * Why: 기상청 단기예보 API는 API 종류마다 발표 주기 · 데이터 제공 지연이 달라
 * 단순한 "현재 시각 - 1시간"으로는 No Data(Error 03)를 자주 맞게 된다.
 * 각 API의 실제 운영 스펙을 상수로 명시해 호출 실패를 줄인다.
 *
 * 참고 스펙(기상청 단기예보 API 운영 가이드)
 * - 초단기실황(getUltraSrtNcst): 매시 정시 발표 · API 제공은 약 40분 후부터
 * - 초단기예보(getUltraSrtFcst): 매시 30분 발표 · API 제공은 약 45분 후부터
 * - 단기예보(getVilageFcst): 02/05/08/11/14/17/20/23시 발표 · 약 10분 후부터 제공하지만
 * 운영 상 수 분~20분 가량 흔들려 여유 있게 30분 후로 잡는다.
 */
public class ApiTimeCalculator {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

	/** 초단기실황 제공 지연 (정시 발표 → +40분 이후부터 응답) */
	private static final int NCST_PROVISION_DELAY_MINUTES = 40;
	/** 초단기예보 제공 지연 (HH:30 발표 → +15분 이후부터 응답) */
	private static final int ULTRA_FCST_PROVISION_DELAY_MINUTES = 15;
	/** 단기예보 제공 지연 (발표 시각 → +30분 이후부터 응답, 운영 진동 대비) */
	private static final int VILAGE_FCST_PROVISION_DELAY_MINUTES = 30;

	/** 단기예보 발표 시각 룩업 (KST, 3시간 간격 총 8회/일) */
	private static final int[] VILAGE_BASE_HOURS = { 2, 5, 8, 11, 14, 17, 20, 23 };

	public record BaseDateTime(String baseDate, String baseTime) {
	}

	/**
	 * 초단기실황 base_date/base_time
	 *
	 * 매시 정시 발표 · 제공 지연 40분을 반영해 현재 시각에서 40분을 뺀 뒤
	 * 시 단위로 내림한다. 분이 40 미만이면 자동으로 한 시간 전 정시로 롤백된다.
	 */
	public static BaseDateTime getNcstBaseDateTime() {
		LocalDateTime base = LocalDateTime.now()
				.minusMinutes(NCST_PROVISION_DELAY_MINUTES)
				.truncatedTo(ChronoUnit.HOURS);

		return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
	}

	/**
	 * 초단기예보 base_date/base_time
	 *
	 * 매시 30분 발표 · 제공 지연 15분을 반영. 현재 시각에서 15분을 뺀 뒤
	 * - 분 < 30 이면 직전 시각의 HH:30
	 * - 분 >= 30 이면 현재 시각의 HH:30
	 * 로 세팅한다.
	 */
	public static BaseDateTime getFcstBaseDateTime() {
		LocalDateTime adjusted = LocalDateTime.now()
				.minusMinutes(ULTRA_FCST_PROVISION_DELAY_MINUTES);
		LocalDateTime base;

		if (adjusted.getMinute() < 30) {
			base = adjusted.minusHours(1).withMinute(30).truncatedTo(ChronoUnit.MINUTES);
		} else {
			base = adjusted.withMinute(30).truncatedTo(ChronoUnit.MINUTES);
		}

		return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
	}

	/**
	 * 단기예보 base_date/base_time
	 *
	 * 하루 8회 불규칙 발표(02/05/08/11/14/17/20/23시) · 제공 지연 30분을 반영.
	 * 현재 시각에서 30분을 뺀 기준으로 가장 최근에 이미 발표된 시각을 찾는다.
	 * 02시 이전이면 전날 23시 발표본을 사용.
	 */
	public static BaseDateTime getVilageFcstBaseDateTime() {
		LocalDateTime adjusted = LocalDateTime.now()
				.minusMinutes(VILAGE_FCST_PROVISION_DELAY_MINUTES);

		for (int i = VILAGE_BASE_HOURS.length - 1; i >= 0; i--) {
			if (adjusted.getHour() >= VILAGE_BASE_HOURS[i]) {
				LocalDateTime base = adjusted
						.withHour(VILAGE_BASE_HOURS[i])
						.withMinute(0)
						.truncatedTo(ChronoUnit.HOURS);
				return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
			}
		}

		// 조정된 현재 시각이 02시 이전 → 전날 23시 발표본 사용
		LocalDateTime base = adjusted.minusDays(1)
				.withHour(23)
				.truncatedTo(ChronoUnit.HOURS);
		return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
	}
}
