package com.sparta.spartatigers.domain.foundation.user.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank String refreshToken
    ) {

}
