package com.sparta.spartatigers.domain.foundation.user.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserMeResponse(
    Long id,
    Properties properties,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
        String nickname,
        @JsonProperty("profile_image") String profileImage,
        @JsonProperty("thumbnail_image") String thumbnailImage
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(String email) {}
}
