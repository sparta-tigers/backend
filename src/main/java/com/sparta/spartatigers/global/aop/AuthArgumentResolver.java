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
        
        // Bearer 접두사 확인 (대소문자 무시)
        if (!bearerToken.toLowerCase().startsWith("bearer ")) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }
        
        // 안전한 토큰 추출 (ArrayIndexOutOfBoundsException 방지)
        String[] parts = bearerToken.split(" ", 2); // 최대 2부분으로 분리
        if (parts.length != 2 || parts[1].isBlank()) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }
        
        String accessToken = parts[1].trim();
        if (accessToken.isBlank()) {
            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
        }

        return tokenService.parseAccessToken(accessToken);
    }
}
