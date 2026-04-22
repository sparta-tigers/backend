package com.sparta.spartatigers.domain.startinglineup.model;

public enum Position  {
	P("투수"),
	C("포수"),
	FIRST_BASE("1루수"),
	SECOND_BASE("2루수"),
	THIRD_BASE("3루수"),
	SS("유격수"),
	LF("좌익수"),
	CF("중견수"),
	RF("우익수"),
	DH("지명타자");

	private final String koreanName;

	Position(String koreanName) {
		this.koreanName = koreanName;
	}

	public static Position fromKorean(String koreanName) {
		for (Position position : values()) {
			// 크롤링 데이터에 공백이 있을 수 있으니 trim()으로 양옆 공백 제거 후 비교
			if (position.koreanName.equals(koreanName.trim())) {
				return position;
			}
		}
		throw new IllegalArgumentException("알 수 없는 포지션입니다: [" + koreanName + "]");
	}
}
