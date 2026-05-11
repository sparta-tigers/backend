package com.sparta.spartatigers.domain.liveboard.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sparta.spartatigers.domain.liveboard.match.model.Match;
import com.sparta.spartatigers.domain.liveboard.model.LineupBatter;
import com.sparta.spartatigers.domain.startinglineup.model.LineupPlayer;
import com.sparta.spartatigers.domain.startinglineup.model.StartingLineup;

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

    /**
     * DB 엔티티로부터 응답 DTO 생성 (Fallback 용)
     */
    public static LineupResponseDto fromEntities(Long matchId, List<StartingLineup> lineups) {
        StartingLineup home = null;
        StartingLineup away = null;

        for (StartingLineup lineup : lineups) {
            Match m = lineup.getMatch();
            if (m.getHomeTeam().getId().equals(lineup.getTeam().getId())) {
                home = lineup;
            } else {
                away = lineup;
            }
        }

        return LineupResponseDto.builder()
                .matchId(matchId)
                .homeTeamName(home != null ? home.getTeam().getName() : null)
                .homeTeamCode(home != null ? home.getTeam().getCode().name() : null)
                .awayTeamName(away != null ? away.getTeam().getName() : null)
                .awayTeamCode(away != null ? away.getTeam().getCode().name() : null)
                .homeBatters(home != null ? fromPlayers(home.getLineupPlayers()) : Collections.emptyList())
                .awayBatters(away != null ? fromPlayers(away.getLineupPlayers()) : Collections.emptyList())
                .build();
    }

    private static List<LineupBatterResponse> fromPlayers(List<LineupPlayer> players) {
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        }
        return players.stream()
                .map((LineupPlayer p) -> LineupBatterResponse.builder()
                        .name(p.getPlayerName())
                        .position(p.getPosition().getKoreanName())
                        .battingOrder(String.valueOf(p.getBattingOrder()))
                        .build())
                .collect(Collectors.toList());
    }
}
