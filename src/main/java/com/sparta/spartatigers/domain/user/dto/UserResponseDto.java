package com.sparta.spartatigers.domain.user.dto;

import com.sparta.spartatigers.domain.user.model.User;

public record UserResponseDto(Long userId, String userNickname) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getId(), user.getNickname());
    }

    public static UserResponseDto from(Long userId, String userNickname) {
        return new UserResponseDto(userId, userNickname);
    }
}
