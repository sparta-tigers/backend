package com.sparta.spartatigers.domain.liveboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MatchScore {
    private int strike;        // 스트라이크 카운트
    private int ball;          // 볼 카운트
    private int out;           // 아웃 카운트
    private String homeScore;  // 홈팀 점수
    private String awayScore;  // 어웨이팀 점수
    private String pitcherCount; // 투구수
}