package com.sparta.spartatigers.domain.foundation.baseball.lineup.dto;

import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupBatter;
import lombok.Builder;
import lombok.Getter;

/**
 * 프론트엔드에 노출할 타자 정보 응답 DTO
 *
 * Why: LineupBatter의 atBats, runs는 프론트에서 불필요하므로 선별 직렬화.
 */
@Getter
@Builder
public class LineupBatterResponse {
    private String battingOrder;
    private String name;
    private String position;
    private int hits;
    private int rbis;

    public static LineupBatterResponse from(LineupBatter batter) {
        return LineupBatterResponse.builder()
                .battingOrder(batter.getBattingOrder())
                .name(batter.getName())
                .position(batter.getPosition())
                .hits(batter.getHits())
                .rbis(batter.getRbis())
                .build();
    }
}
