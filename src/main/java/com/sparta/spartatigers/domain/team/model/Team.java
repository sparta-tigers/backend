package com.sparta.spartatigers.domain.team.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity(name = "teams")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column
    private String name;

    @Column
    @Enumerated(value = EnumType.STRING)
    private TeamCode code;

    @Column private String path;
}