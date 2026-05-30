package com.sparta.spartatigers.global.firebase.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank String fcmToken) {

}
