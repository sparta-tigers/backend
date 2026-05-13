package com.sparta.spartatigers.domain.liveboard.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column
    private int nx;

    @Column
    private int ny;

}