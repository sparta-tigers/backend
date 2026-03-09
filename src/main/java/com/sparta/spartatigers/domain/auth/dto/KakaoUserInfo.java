package com.sparta.spartatigers.domain.auth.dto;

public record KakaoUserInfo(
    String id,
    String email,
    String nickname,
    String profileImageUrl
) {

}
