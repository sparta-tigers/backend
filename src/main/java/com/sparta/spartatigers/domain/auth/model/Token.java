package com.sparta.spartatigers.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
@AllArgsConstructor
public class Token {

    private String accessToken;

    private Date accessTokenIssuedAt;

    private Date accessTokenExpiredAt;

    private String refreshToken;

    private Date refreshTokenIssuedAt;

    private Date refreshTokenExpiredAt;

}