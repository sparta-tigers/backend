package com.sparta.spartatigers.domain.liveboard.match.model;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.RequiredArgsConstructor;

/**
 * 경기 장소 타입 (홈/어웨이)
 */
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
