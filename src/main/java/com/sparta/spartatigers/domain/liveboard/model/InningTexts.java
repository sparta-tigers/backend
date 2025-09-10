package com.sparta.spartatigers.domain.liveboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InningTexts {
    private List<String> inningOneTexts;   // 1회 문자중계
    private List<String> inningTwoTexts;   // 2회 문자중계
    private List<String> inningThreeTexts; // 3회 문자중계
    private List<String> inningFourTexts;  // 4회 문자중계
    private List<String> inningFiveTexts;  // 5회 문자중계
    private List<String> inningSixTexts;   // 6회 문자중계
    private List<String> inningSevenTexts; // 7회 문자중계
    private List<String> inningEightTexts; // 8회 문자중계
    private List<String> inningNineTexts;  // 9회 문자중계
    private List<String> inningExtraTexts; // 연장 문자중계
}