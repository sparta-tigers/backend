package com.sparta.spartatigers.domain.weather.model;

public enum RainType {
	NONE(0, "없음"),
	RAIN(1, "비"),
	RAIN_SNOW(2, "비/눈"),
	SNOW(3, "눈"),
	RAINDROP(5, "빗방울"),
	RAINDROP_SNOW_FLYING(6, "빗방울눈날림"),
	SNOW_FLYING(7, "눈날림");

	private final int code;
	private final String description;

	RainType(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 기상청 PTY 코드 → RainType.
	 *
	 * Why: 기존 구현은 null/미상 코드를 무조건 NONE(강수없음)으로 변환해,
	 * 업스트림 에러/파싱 실패 시에도 "강수없음"으로 오염되는 문제가 있었다.
	 * 이제는 "실제 0 응답"만 NONE, 미상은 null로 분리한다.
	 */
	public static RainType fromCode(String code) {
		if (code == null)
			return null;
		return switch (code) {
			case "0" -> NONE;
			case "1" -> RAIN;
			case "2" -> RAIN_SNOW;
			case "3" -> SNOW;
			case "5" -> RAINDROP;
			case "6" -> RAINDROP_SNOW_FLYING;
			case "7" -> SNOW_FLYING;
			default -> null;
		};
	}

}
