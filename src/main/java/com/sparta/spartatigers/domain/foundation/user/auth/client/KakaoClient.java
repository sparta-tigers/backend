package com.sparta.spartatigers.domain.foundation.user.auth.client;

import com.sparta.spartatigers.domain.foundation.user.auth.dto.KakaoTokenResponse;
import com.sparta.spartatigers.domain.foundation.user.auth.dto.KakaoUserInfo;
import com.sparta.spartatigers.domain.foundation.user.auth.dto.KakaoUserMeResponse;
import com.sparta.spartatigers.global.config.KakaoOauthProperties;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoClient {

    private final RestClient kakaoRestClient;
    private final KakaoOauthProperties properties;

    public String getAccessToken(String code, String redirectUri) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "authorization_code");
            form.add("client_id", properties.clientId());
            form.add("redirect_uri", redirectUri);
            form.add("code", code);

            if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
                form.add("client_secret", properties.clientSecret());
            }

            KakaoTokenResponse res = kakaoRestClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);

            if (res == null || res.accessToken() == null || res.accessToken().isBlank()) {
                throw new InvalidRequestException(ExceptionCode.OAUTH_TOKEN_EXCHANGE_FAILED);
            }
            return res.accessToken();

        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao token exchange failed: {}", e.getMessage(), e);
            throw new InvalidRequestException(ExceptionCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {

        KakaoUserMeResponse response = kakaoRestClient.get()
            .uri(properties.userInfoUri())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .body(KakaoUserMeResponse.class);

        if (response == null || response.id() == null) {
            throw new InvalidRequestException(ExceptionCode.OAUTH_USERINFO_FAILED);
        }

        return new KakaoUserInfo(
            String.valueOf(response.id()),
            response.kakaoAccount() != null ? response.kakaoAccount().email() : null,
            response.properties() != null ? response.properties().nickname() : null,
            response.properties() != null ? response.properties().profileImage() : null
        );
    }
}