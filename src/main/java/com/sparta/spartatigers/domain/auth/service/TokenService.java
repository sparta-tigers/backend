package com.sparta.spartatigers.domain.auth.service;

import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;

public interface TokenService {

    Token generateToken(TokenClaim tokenClaim);

    TokenClaim parseAccessToken(final String accessToken);

    TokenClaim parseRefreshToken(final String refreshToken);

}