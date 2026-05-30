package com.sparta.spartatigers.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOauthProperties(
          String clientId,
          String clientSecret,
          String tokenUri,
          String userInfoUri) {

}
