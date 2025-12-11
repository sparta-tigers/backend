package com.sparta.spartatigers.domain.favoriteteam.model.entity;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 대리키 필요없으므로 userid를 PK로
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "team_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public static FavoriteTeam of(FavoriteTeam favoriteTeam) {
        return new FavoriteTeam(favoriteTeam.getUser().getId(), favoriteTeam.getUser(), favoriteTeam.getTeam());
    }

    public static FavoriteTeam from(User user, Team team) {
        FavoriteTeam favoriteTeam = new FavoriteTeam();
        favoriteTeam.user = user;
        favoriteTeam.team = team;
        return favoriteTeam;
    }

    public void update(Team team) {
        this.team = team;
    }
}