package com.sparta.spartatigers.domain.liveboard.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sparta.spartatigers.domain.liveboard.model.LineupBatter;
import lombok.Builder;
import lombok.Getter;

/**
 * 라인업 조회 API 응답 DTO
 *
 * Why: matchId 기반으로 홈/어웨이 양 팀 라인업을 한 번에 내려주기 위한 응답 구조.
 * 프론트엔드의 MatchLineupDto와 1:1 매핑.
 */
@Getter
@Builder
public class LineupResponseDto {
    private Long matchId;
    private String homeTeamName;
    private String homeTeamCode;
    private String awayTeamName;
    private String awayTeamCode;
    private List<LineupBatterResponse> homeBatters;
    private List<LineupBatterResponse> awayBatters;

    /**
     * Redis 캐시에 데이터가 없을 때 빈 응답 생성
     */
    public static LineupResponseDto empty(Long matchId) {
        return LineupResponseDto.builder()
                .matchId(matchId)
                .homeTeamName(null)
                .homeTeamCode(null)
                .awayTeamName(null)
                .awayTeamCode(null)
                .homeBatters(Collections.emptyList())
                .awayBatters(Collections.emptyList())
                .build();
    }

    /**
     * LineupCacheDto로부터 응답 DTO 생성
     */
    public static LineupResponseDto from(LineupCacheDto cache) {
        return LineupResponseDto.builder()
                .matchId(cache.getMatchId())
                .homeTeamName(null)
                .homeTeamCode(null)
                .awayTeamName(null)
                .awayTeamCode(null)
                .homeBatters(toBatterResponses(cache.getHomeBatters()))
                .awayBatters(toBatterResponses(cache.getAwayBatters()))
                .build();
    }

    private static List<LineupBatterResponse> toBatterResponses(List<LineupBatter> batters) {
        if (batters == null || batters.isEmpty()) {
            return Collections.emptyList();
        }
        return batters.stream()
                .map(LineupBatterResponse::from)
                .collect(Collectors.toList());
    }
}
