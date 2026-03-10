package com.sparta.spartatigers.domain.notification.dto;

/**
 * 클라이언트에서 전달하는 디바이스(푸시) 토큰 등록 요청 DTO.
 * Java 관점에서는 단순 DTO, TypeScript의 interface와 동일한 역할을 한다.
 */
public record DeviceTokenRequest(
    String token,
    String deviceType
) {
}

