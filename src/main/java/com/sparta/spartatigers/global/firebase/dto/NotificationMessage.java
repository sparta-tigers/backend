package com.sparta.spartatigers.global.firebase.dto;

public record NotificationMessage(
        String token,
        String title,
        String body) {

    public static NotificationMessage of(String token, String title, String body) {
        return new NotificationMessage(token, title, body);
    }

    public boolean hasValidToken() {
        return token != null && !token.isBlank();
    }
}
