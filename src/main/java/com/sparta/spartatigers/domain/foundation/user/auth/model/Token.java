package com.sparta.spartatigers.domain.foundation.user.auth.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
