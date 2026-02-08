package com.sparta.spartatigers.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostseasonStage {
	WILD_CARD("와일드 카드"), // 와일드 카드
	SEMI_PLAYOFF("준플레이오프"), // 준플레이오프
	PLAYOFF("플레이오프"), // 플레이오프
	KOREAN_SERIES ("한국시리즈")//한국시리즈
	;

	private final String krName;
}
