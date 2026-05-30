package com.sparta.spartatigers.domain.support.weather.model;

import java.util.Arrays;

public enum WindDirection {
	N(0, "북"), // 변환값 16인 경우도 N
	NNE(1, "북북동"),
	NE(2, "북동"),
	ENE(3, "동북동"),
	E(4, "동"),
	ESE(5, "동남동"),
	SE(6, "남동"),
	SSE(7, "남남동"),
	S(8, "남"),
	SSW(9, "남남서"),
	SW(10, "남서"),
	WSW(11, "서남서"),
	W(12, "서"),
	WNW(13, "서북서"),
	NW(14, "북서"),
	NNW(15, "북북서");

	private final int code;
	private final String description;

	WindDirection(int code, String description) {
		this.code = code;
		this.description = description;
	}

	// # 풍향값에 따른 16방위 변환식
	// (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	public static WindDirection fromDegree(double degree) {
		int code = (int) (Math.floor((degree + 11.25) / 22.5));
		if (code == 16)
			code = 0;
		final int finalCode = code;

		return Arrays.stream(WindDirection.values())
				.filter(windDirection -> windDirection.code == finalCode)
				.findFirst()
				.orElseThrow();
	}
}
