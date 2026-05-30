package com.sparta.spartatigers.domain.foundation.user.auth.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.foundation.user.auth.model.RefreshToken;
import com.sparta.spartatigers.domain.foundation.user.auth.model.Token;
import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.foundation.user.auth.repository.RefreshTokenRepository;
import com.sparta.spartatigers.domain.foundation.user.account.model.UserRole;
import com.sparta.spartatigers.global.config.JwtConfig;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
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
        // [FIX] 문제 1-1: catch(Exception) → JWT 관련 예외만 좌게 잡아 NPE 등 런타임 버그가 UNAUTHORIZED로 둔갑하는 문제 수정
        // JwtException: ExpiredJwtException, MalformedJwtException, SignatureException 등을 모두 포함하는 부모 타입
        } catch (JwtException | IllegalArgumentException e) {
            // [FIX] 문제 1-2: e.getMessage()만 로깅하면 스택트레이스 손실 — SLF4J 관례대로 마지막 인자에 e 전달
            log.warn("JWT 토큰 파싱 실패 [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            // [FIX] 문제 1-3: FQN 제거 — 파일 상단 import로 정리
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }
    }

    @Override
    public TokenClaim parseRefreshToken(String refreshToken) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getRefreshToken().secret().getBytes());
            Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken);

            final String subject = claimsJws.getPayload().getSubject();

            return TokenClaim.builder()
                .subject(subject)
                .build();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Refresh 토큰 파싱 실패 [{}]: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new InvalidRequestException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }
    }

}