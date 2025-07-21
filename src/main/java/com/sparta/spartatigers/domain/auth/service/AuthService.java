package com.sparta.spartatigers.domain.auth.service;

import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.user.model.LoginUser;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.error.CustomException;
import com.sparta.spartatigers.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                        new CustomException(ErrorType.VALIDATION_ERROR, String.format("이미 존재하는 유저입니다. [%s]", email)));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorType.AUTHENTICATION_ERROR, "이메일과 비밀번호를 다시 확인 해주세요");
        }


        TokenClaim tokenClaim = TokenClaim.from(user);
        return tokenService.generateToken(tokenClaim);
    }
}
