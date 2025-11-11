package com.sparta.spartatigers.domain.liveboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveBoardData {
    private Long matchId;
    private List<Player> players;       // 선수 목록
    private MatchScore matchScore;      // 경기 점수 정보
    private InningTexts inningTexts;    // 이닝별 문자중계
    private String currentInning;       // 현재 이닝
    private String[] awayInningScores;  // 어웨이팀 이닝별 점수 (1-15회)
    private String[] homeInningScores;  // 홈팀 이닝별 점수 (1-15회)
    private List<LineupBatter> awayBatters;
    private List<LineupBatter> homeBatters;
}