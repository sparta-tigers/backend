package com.sparta.spartatigers.domain.weather.api;

import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ApiTimeCalculator {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

	public record BaseDateTime(String baseDate, String baseTime) {}

	// 초단기실황 베이스타임 생성 메서드 ( 매시간 정시 ,  10분 딜레이 )
	public static BaseDateTime getNcstBaseDateTime() {
		LocalDateTime minusUpdateTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime base = minusUpdateTime.truncatedTo(ChronoUnit.HOURS);

		String baseDate = base.format(DATE_FORMAT);
		String baseTime = base.format(TIME_FORMAT);

		return new BaseDateTime(baseDate, baseTime);
	}

	// 초단기예보 베이스타임 생성 메서드 ( 매시간 30분 ,  15분 딜레이 )
	public static BaseDateTime getFcstBaseDateTime() {
		LocalDateTime minusUpdateTime = LocalDateTime.now().minusMinutes(15);
		LocalDateTime base;

		if(minusUpdateTime.getMinute() < 30) {
			base = minusUpdateTime.minusHours(1).withMinute(30).truncatedTo(ChronoUnit.MINUTES);
		} else {
			base = minusUpdateTime.withMinute(30).truncatedTo(ChronoUnit.MINUTES);
		}

		String baseDate = base.format(DATE_FORMAT);
		String baseTime = base.format(TIME_FORMAT);

		return new BaseDateTime(baseDate, baseTime);
	}

	// 단기예보 베이스타임 생성 메서드 ( 베이스 타임 1일 8회, 10분 딜레이 )
	public static BaseDateTime getVilageFcstBaseDateTime() {
		LocalDateTime minusUpdateTime = LocalDateTime.now().minusMinutes(10);
		LocalDateTime base = minusUpdateTime.truncatedTo(ChronoUnit.HOURS);

		int[] hours = {2, 5, 8, 11, 14, 17, 20, 23};

		for(int i = hours.length -1 ; i >= 0 ; i--) {
			if(minusUpdateTime.getHour() >= hours[i]) {
				base = minusUpdateTime.withHour(hours[i]).withMinute(0);
				return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
			}
		}

		base = minusUpdateTime.minusDays(1).withHour(23).truncatedTo(ChronoUnit.HOURS);
		return new BaseDateTime(base.format(DATE_FORMAT), base.format(TIME_FORMAT));
	}
}
