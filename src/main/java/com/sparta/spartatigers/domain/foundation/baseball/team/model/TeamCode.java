package com.sparta.spartatigers.domain.foundation.baseball.team.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamCode {
    LG("LG"),
    KT("KT"),
    OB("DOOSAN"),
    HT("KIA"),
    SS("SAMSUNG"),
    LT("LOTTE"),
    NC("NC"),
    SK("SSG"),
    HH("HANWHA"),
    WO("KIWOOM");

    private final String descriptiveCode;
}
