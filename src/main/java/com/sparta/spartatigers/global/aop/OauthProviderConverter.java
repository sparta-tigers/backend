package com.sparta.spartatigers.global.aop;

import com.sparta.spartatigers.domain.auth.model.OAuthProvider;
import org.springframework.core.convert.converter.Converter;

public class OauthProviderConverter implements Converter<String, OAuthProvider> {

    @Override
    public OAuthProvider convert(String source) {
        return OAuthProvider.fromString(source);
    }
}
