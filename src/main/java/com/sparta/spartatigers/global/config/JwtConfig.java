package com.sparta.spartatigers.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private final AccessToken accessToken;

    private final RefreshToken refreshToken;

    public record AccessToken(String secret, long expire) {
    }

    public record RefreshToken(String secret, long expire) {
    }

}