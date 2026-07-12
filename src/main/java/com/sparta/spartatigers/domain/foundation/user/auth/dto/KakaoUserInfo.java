package com.sparta.spartatigers.domain.foundation.user.auth.dto;

public record KakaoUserInfo(
    String id,
    String email,
    String nickname,
    String profileImageUrl
) {}
