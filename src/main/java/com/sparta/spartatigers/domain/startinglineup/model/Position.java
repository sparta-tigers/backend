package com.sparta.spartatigers.domain.startinglineup.model;

public enum Position  {
	P("투수", "1"),
	C("포수", "2"),
	FIRST_BASE("1루수", "3"),
	SECOND_BASE("2루수", "4"),
	THIRD_BASE("3루수", "5"),
	SS("유격수", "6"),
	LF("좌익수", "7"),
	CF("중견수", "8"),
	RF("우익수", "9"),
	DH("지명타자", "10");

	private final String koreanName;
	private final String code;

	Position(String koreanName, String code) {
		this.koreanName = koreanName;
		this.code = code;
	}

	public String getKoreanName() {
		return koreanName;
	}

	public static Position fromKorean(String positionStr) {
		if (positionStr == null) {
			throw new IllegalArgumentException("포지션 정보가 없습니다.");
		}

		String trimmed = positionStr.trim();
		
		for (Position position : values()) {
			// 1. 한글 명칭 비교 (예: "우익수")
			if (position.koreanName.equals(trimmed)) {
				return position;
			}
			// 2. 숫자 코드 비교 (예: "9")
			if (position.code.equals(trimmed)) {
				return position;
			}
		}
		
		throw new IllegalArgumentException("알 수 없는 포지션입니다: [" + positionStr + "]");
	}
}
