package com.sparta.spartatigers.domain.foundation.baseball.lineup.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "match_team_batting_order",
            columnNames = { "match_id", "team_id", "batting_order" }
        ),
    }
)
public class LineupPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        {
            @JoinColumn(name = "match_id", referencedColumnName = "match_id"),
            @JoinColumn(name = "team_id", referencedColumnName = "team_id"),
        }
    )
    private StartingLineup startingLineup;

    private int battingOrder;

    private String playerName;

    @Enumerated(EnumType.STRING)
    private Position position;

    private LineupPlayer(
        int battingOrder,
        Position position,
        String playerName
    ) {
        this.battingOrder = battingOrder;
        this.position = position;
        this.playerName = playerName;
    }

    public static LineupPlayer of(
        int battingOrder,
        String positionStr,
        String playerName
    ) {
        return new LineupPlayer(
            battingOrder,
            Position.fromKorean(positionStr),
            playerName
        );
    }

    public void setStartingLineup(StartingLineup startingLineup) {
        this.startingLineup = startingLineup;
    }
}
