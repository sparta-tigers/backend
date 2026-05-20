package com.sparta.spartatigers.domain.liveboard.dto;

import com.sparta.spartatigers.domain.liveboard.model.InningTexts;
import com.sparta.spartatigers.domain.liveboard.model.LiveBoardData;
import com.sparta.spartatigers.domain.liveboard.model.MatchScore;
import com.sparta.spartatigers.domain.liveboard.model.Player;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 프론트엔드로 전달할 경량화된 실시간 중계 데이터 DTO
 * 
 * Why: 크롤러 원본 데이터인 LiveBoardData에는 프론트엔드 Liveboard 탭에서 사용하지 않는
 * 라인업(awayBatters, homeBatters)과 이닝별 점수(awayInningScores, homeInningScores) 배열이 포함되어 있습니다.
 * 네트워크 트래픽 절약과 프론트엔드의 스키마 일치를 위해 필수 데이터만 추출합니다.
 */
@Getter
@Builder
public class LiveBoardDataResponseDto {
    private Long matchId;
    private MatchScore matchScore;
    private InningTexts inningTexts;
    private String currentInning;
    private List<Player> players;

    public static LiveBoardDataResponseDto from(LiveBoardData data) {
        if (data == null) {
            return null;
        }
        return LiveBoardDataResponseDto.builder()
                .matchId(data.getMatchId())
                .matchScore(data.getMatchScore())
                .inningTexts(data.getInningTexts())
                .currentInning(data.getCurrentInning())
                .players(data.getPlayers() == null ? null : new ArrayList<>(data.getPlayers()))
                .build();
    }
}
