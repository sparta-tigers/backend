package com.sparta.spartatigers.domain.support.weather.model;

public enum SkyStatus {
	SUNNY(1, "맑음"),
	CLOUDY_PARTLY(3, "구름많음"),
	CLOUDY(4, "흐림");

	private final int code;
	private final String description;

	SkyStatus(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * 기상청 SKY 코드 → SkyStatus.
	 *
	 * Why: 기존 구현은 null/미상 코드를 무조건 SUNNY로 변환해,
	 * 업스트림 에러/파싱 실패 시에도 UI에 "맑음"이 표시되는 오염이 있었다.
	 * 이제는 미상을 null로 반환해 상위 계층이 "데이터 없음"으로 명확히 취급하도록 한다.
	 */
	public static SkyStatus fromCode(String code) {
		if (code == null)
			return null;
		return switch (code) {
			case "1" -> SUNNY;
			case "3" -> CLOUDY_PARTLY;
			case "4" -> CLOUDY;
			default -> null;
		};
	}
}
