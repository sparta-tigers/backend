package com.sparta.spartatigers.domain.foundation.baseball.match.model;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.Stadium;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "matches")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeagueType leagueType;

    @Column(nullable = false)
    private int seasonYear;

    @Column
    private LocalDateTime matchTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Column
    @Enumerated(value = EnumType.STRING)
    private MatchResult matchResult;

    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Column
    private String remark; // 비고

    public void updateScore(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
        }
        this.homeScore = homeScore;
        this.awayScore = awayScore;

        validateMatchTimeState(this.matchResult);

        // 경기 시작 전(NOT_PLAYED)에 스코어가 업데이트되면 진행 중(PROCEEDING) 상태로 변환합니다.
        // 단, 경기 예정 시간(matchTime) 이전에는 진행 중(PROCEEDING)으로 변환할 수 없습니다.
        if (this.matchResult == MatchResult.NOT_PLAYED
                && this.matchTime != null
                && !LocalDateTime.now().isBefore(this.matchTime)) {
            this.matchResult = MatchResult.PROCEEDING;
        }
    }

    public void updateFinalResult(MatchResult result, int homeScore, int awayScore) {
        validateMatchTimeState(result);
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
        }
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.matchResult = result;
    }

    private void validateMatchTimeState(MatchResult targetResult) {
        if (targetResult != null && this.matchTime != null && LocalDateTime.now().isBefore(this.matchTime)) {
            if (targetResult != MatchResult.CANCEL && targetResult != MatchResult.NOT_PLAYED) {
                throw new IllegalStateException(
                        "경기 예정 시간 이전에는 CANCEL 또는 NOT_PLAYED 상태만 가질 수 있습니다. (요청 상태: " + targetResult + ")");
            }
        }
    }
}