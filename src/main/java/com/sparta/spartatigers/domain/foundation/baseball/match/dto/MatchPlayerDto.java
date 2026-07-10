package com.sparta.spartatigers.domain.foundation.baseball.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayerDto {
    private String role; // 선수 역할/포지션
    private String name; // 선수 이름
}