package com.sparta.spartatigers.domain.weather.model;

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

	public static SkyStatus fromCode (String code) {
		if(code == null) return SUNNY;
		return switch (code) {
			case "1" -> SUNNY;
			case "3" -> CLOUDY_PARTLY;
			case "4" -> CLOUDY;
			default -> SUNNY;
		};
	}
}
