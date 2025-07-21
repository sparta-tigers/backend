package com.sparta.spartatigers.domain.auth.service;

import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.user.model.UserRole;
import com.sparta.spartatigers.global.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {

    private final JwtConfig jwtConfig;

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
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getAccessToken().secret().getBytes());
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

        final String email = claimsJws.getPayload().getSubject();
        // 한번에 Long으로 받으면에러가 발생함 Number.class로 가져와야 안전
        final Number userId = claimsJws.getPayload().get("userId", Number.class);
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