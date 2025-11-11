package com.sparta.spartatigers.domain.auth.service;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public Token login(final String email, final String password) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new InvalidRequestException(ExceptionCode.EMAIL_ALREADY_USED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidRequestException(ExceptionCode.INVALID_PASSWORD);
        }


        TokenClaim tokenClaim = TokenClaim.from(user);
        return tokenService.generateToken(tokenClaim);
    }
}
