package com.sparta.spartatigers.domain.foundation.user.auth.service;

import com.sparta.spartatigers.domain.foundation.user.auth.model.Token;
import com.sparta.spartatigers.global.aop.TokenClaim;

public interface TokenService {

    Token generateToken(TokenClaim tokenClaim);

    TokenClaim parseAccessToken(final String accessToken);

    TokenClaim parseRefreshToken(final String refreshToken);

}