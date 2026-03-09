package com.sparta.spartatigers.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank String refreshToken
    ) {

}
