package com.sparta.spartatigers.domain.team.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "stadiums")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Stadium extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stadium_id")
    private Long id;

    @Column
    private String name;

    @Column
    private double latitude;

    @Column
    private double longitude;
}