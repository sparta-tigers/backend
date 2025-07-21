package com.sparta.spartatigers.domain.auth.model;

public enum OAuthProvider {
    KAKAO,
    GOOGLE;

    public static OAuthProvider fromString(String provider) {
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> KAKAO;
            case "GOOGLE" -> GOOGLE;

            default -> throw new IllegalArgumentException(String.format("%s는 지원하지 않는 oauth 프로바이더 입니다.", provider));
        };
    }
}
