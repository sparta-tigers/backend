package com.sparta.spartatigers.domain.foundation.baseball.lineup.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineupBatter {
    private String battingOrder; // 순서
    private String position; // 포지션
    private String name; // 이름
    private int atBats; // 타수
    private int runs; // 득점
    private int hits; // 안타
    private int rbis; // 타점
}