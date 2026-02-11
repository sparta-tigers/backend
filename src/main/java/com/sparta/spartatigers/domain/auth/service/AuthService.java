package com.sparta.spartatigers.domain.auth.service;

import com.sparta.spartatigers.domain.auth.client.KakaoClient;
import com.sparta.spartatigers.domain.auth.dto.KakaoUserInfo;
import com.sparta.spartatigers.domain.auth.model.OAuthProvider;
import com.sparta.spartatigers.domain.auth.model.Oauth;
import com.sparta.spartatigers.domain.auth.repository.OauthRepository;
import com.sparta.spartatigers.domain.user.model.UserRole;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.auth.model.Token;
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OauthRepository oauthRepository;
    private final KakaoClient kakaoClient;
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

    @Transactional
    public Token kakaoLogin(String code, String redirectUri) {

        String accessToken = kakaoClient.getAccessToken(code, redirectUri);

        KakaoUserInfo info = kakaoClient.getUserInfo(accessToken);

        if (info.email() == null || info.email().isBlank()) {
            throw new InvalidRequestException(ExceptionCode.OAUTH_EMAIL_REQUIRED);
        }

        User user = oauthRepository
            .findByProviderAndProviderId(OAuthProvider.KAKAO, info.id())
            .map(Oauth::getUser)
            .orElseGet(() -> findOrCreateKakaoUser(info));

        TokenClaim tokenClaim = TokenClaim.from(user);
        return tokenService.generateToken(tokenClaim);
    }

    private User findOrCreateKakaoUser(KakaoUserInfo info) {
        return userRepository.findByEmail(info.email())
            .map(existingUser -> {
                if (!oauthRepository.existsByUserAndProvider(existingUser, OAuthProvider.KAKAO)) {
                    oauthRepository.save(
                        Oauth.builder()
                            .provider(OAuthProvider.KAKAO)
                            .providerId(info.id())
                            .user(existingUser)
                            .build()
                    );
                }
                return existingUser;
            })
            .orElseGet(() -> createKakaoUser(info));
    }

    private User createKakaoUser(KakaoUserInfo info) {

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.hash(randomPassword);

        User user = userRepository.save(
            User.builder()
                .email(info.email())
                .password(encodedPassword)
                .nickname(info.nickname())
                .profileImageUrl(info.profileImageUrl())
                .role(UserRole.ROLE_USER)
                .build()
        );

        oauthRepository.save(
            Oauth.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(info.id())
                .user(user)
                .build()
        );

        return user;
    }
}
