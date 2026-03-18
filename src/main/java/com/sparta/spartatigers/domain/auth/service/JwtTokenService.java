package com.sparta.spartatigers.domain.auth.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.auth.model.RefreshToken;
import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.auth.repository.RefreshTokenRepository;
import com.sparta.spartatigers.domain.user.model.UserRole;
import com.sparta.spartatigers.global.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {

    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Token generateToken(TokenClaim tokenClaim) {
        // AccessToken 발급 준비
        final long now = System.currentTimeMillis();
        Date accessTokenExpireAt = new Date(now + jwtConfig.getAccessToken().expire());
        SecretKey accessTokenSecretKey = Keys.hmacShaKeyFor(jwtConfig.getAccessToken().secret().getBytes());
        // refreshToken 발급 준비
        Date refreshTokenExpireAt = new Date(now + jwtConfig.getRefreshToken().expire());
        SecretKey refreshTokenSecret = Keys.hmacShaKeyFor(jwtConfig.getRefreshToken().secret().getBytes());

        Date nowDate = new Date(now);

        final String accessToken = Jwts.builder()
            .subject(tokenClaim.getSubject())
            .claim("userId", tokenClaim.getUserId())
            .claim("email", tokenClaim.getEmail())
            .claim("nickname", tokenClaim.getNickname())
            .claim("profileImageUrl", tokenClaim.getProfileImageUrl())
            .claim("role", tokenClaim.getRole())
            .issuedAt(nowDate)
            .expiration(accessTokenExpireAt)
            .signWith(accessTokenSecretKey)
            .compact();

        final String refreshToken = Jwts.builder()
            .subject(tokenClaim.getSubject())
            .issuedAt(nowDate)
            .expiration(refreshTokenExpireAt)
            .signWith(refreshTokenSecret)
            .compact();

        long ttlSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtConfig.getRefreshToken().expire());
        log.info("refresh expire raw={}, ttlSeconds={}",
            jwtConfig.getRefreshToken().expire(), ttlSeconds);
        refreshTokenRepository.save(
            RefreshToken.builder()
                .token(refreshToken)
                .subject(tokenClaim.getSubject())
                .ttlSeconds(ttlSeconds)
                .build()
        );

        return Token.builder()
            .accessToken(accessToken)
            .accessTokenExpiredAt(accessTokenExpireAt)
            .accessTokenIssuedAt(nowDate)
            .refreshToken(refreshToken)
            .refreshTokenExpiredAt(refreshTokenExpireAt)
            .refreshTokenIssuedAt(nowDate)
            .build();
    }

    @Override
    public TokenClaim parseAccessToken(final String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getAccessToken().secret().getBytes());
            Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

            // 한번에 Long으로 받으면에러가 발생함 Number.class로 가져와야 안전
            final Number userId = claimsJws.getPayload().get("userId", Number.class);
            final String email = claimsJws.getPayload().get("email", String.class);
            final String nickname = claimsJws.getPayload().get("nickname", String.class);
            final String profileImageUrl = claimsJws.getPayload().get("profileImageUrl", String.class);
            final String userRole = claimsJws.getPayload().get("role", String.class);

            return TokenClaim.builder()
                .subject(email)
                .userId(userId.longValue())
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(UserRole.from(userRole))
                .build();
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new com.sparta.spartatigers.global.exception.internal.InvalidRequestException(
                com.sparta.spartatigers.global.exception.enums.ExceptionCode.UNAUTHORIZED
            );
        }
    }

    @Override
    public TokenClaim parseRefreshToken(String refreshToken) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getRefreshToken().secret().getBytes());
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken);

        final String subject = claimsJws.getPayload().getSubject();

        return TokenClaim.builder()
            .subject(subject)
            .build();
    }

}