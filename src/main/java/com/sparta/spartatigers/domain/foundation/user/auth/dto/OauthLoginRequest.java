package com.sparta.spartatigers.domain.foundation.user.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OauthLoginRequest(
    @NotBlank String code,
    @NotBlank String redirectUri
) {}
