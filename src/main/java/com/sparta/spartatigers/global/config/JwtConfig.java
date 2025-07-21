package com.sparta.spartatigers.global.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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