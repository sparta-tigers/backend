package com.sparta.spartatigers.domain.match.model;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 경기 장소 타입 (홈/어웨이)
 */
@Getter
@RequiredArgsConstructor
public enum HomeAway {
    HOME("H"),
    AWAY("A");

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }
}
