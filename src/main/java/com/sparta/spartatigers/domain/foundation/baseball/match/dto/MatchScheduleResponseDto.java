package com.sparta.spartatigers.domain.foundation.baseball.match.dto;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.HomeAway;
import lombok.Builder;
import lombok.Getter;

/**
 * 경기 일정 응답을 위한 DTO.
 * 프론트엔드의 캘린더 UI에서 사용되며, 사용자의 응원 팀 입장에서 상대 팀 정보를 가공하여 전달함.
 */
@Getter
@Builder
public class MatchScheduleResponseDto {

    private Long matchId; // 경기 ID (🚨 추가)
    private Integer day; // 경기 일 (1-31)
    private String opponentCode; // 상대 팀 코드 (예: "OB", "HT", "LG")
    private HomeAway location; // 홈/어웨이 여부 ("H" or "A")
    private String timeText; // 경기 시작 시간 (예: "18:30")
}
