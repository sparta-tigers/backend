package com.sparta.spartatigers.domain.liveboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String role;  // 선수 역할/포지션
    private String name;  // 선수 이름
}