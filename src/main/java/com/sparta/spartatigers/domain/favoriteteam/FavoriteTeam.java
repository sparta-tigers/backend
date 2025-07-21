package com.sparta.spartatigers.domain.favoriteteam;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "favorite_team")
@Table(
        name = "favorite_team",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"})})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteTeam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "team_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
}