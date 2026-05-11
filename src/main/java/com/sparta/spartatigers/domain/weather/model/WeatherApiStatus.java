package com.sparta.spartatigers.domain.weather.model;

/**
 * 기상청 API 응답의 상위 상태 분류
 *
 * Why: 기상청은 점검/장애 시 header.resultCode로 01(Application Error), 03(No Data) 등을
 * 반환하는데, 기존 로직은 이를 "빈 응답"과 동일하게 다뤄 UI가 "맑음/강수없음"으로 오염됐다.
 * 응답 단계에서 원인을 분류해두면 상위 서비스/DTO가 사용자에게 상황을 정확히 전달할 수 있다.
 *
 * 기상청 단기예보 API 공식 resultCode 매핑:
 * - 00: NORMAL_SERVICE (정상)
 * - 01: APPLICATION_ERROR
 * - 02: DB_ERROR
 * - 03: NODATA_ERROR
 * - 04: HTTP_ERROR
 * - 05: SERVICETIME_OUT
 * - 10~99: 인증/파라미터/할당량 등 클라이언트·서버 오류
 */
public enum WeatherApiStatus {

    /** 정상 응답 + 데이터 존재 */
    SUCCESS,
    /** 정상 응답이지만 데이터가 비어있음(00 + item 없음, 또는 03 NODATA_ERROR) */
    NO_DATA,
    /** 기상청 서버측 장애(01, 02, 04, 05 등) */
    UPSTREAM_ERROR,
    /** 우리 서버에서 호출/파싱 단계에서 실패(타임아웃, 연결 오류 등) */
    INTERNAL_ERROR;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
