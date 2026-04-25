package com.sparta.spartatigers.global.aop;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.auth.service.TokenService;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenService tokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class)
                && parameter.getParameterType().equals(TokenClaim.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }
        
        // [FIX] toLowerCase().startsWith() → regionMatches(true, ...) 로 교체
        // 이유: toLowerCase()는 매 호출마다 새 String 객체를 할당 — regionMatches는 할당 없이 직접 비교
        if (!bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }

        // 안전한 토큰 추출 (ArrayIndexOutOfBoundsException 방지)
        String[] parts = bearerToken.split(" ", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }

        // [FIX] trim().isBlank() 중복 검사 제거
        // parts[1].isBlank() 통과 시 trim()해도 blank 불가 — 도달 불가능한 분기 제거
        String accessToken = parts[1].trim();

        return tokenService.parseAccessToken(accessToken);
    }
}
