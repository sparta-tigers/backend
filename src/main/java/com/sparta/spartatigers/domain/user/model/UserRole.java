package com.sparta.spartatigers.domain.user.model;

import com.sparta.spartatigers.global.error.CustomException;
import com.sparta.spartatigers.global.error.ErrorType;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    public static UserRole from(String value) {
        for (UserRole role: values()) {
            if (role.toString().equals(value.toUpperCase())) {
                return role;
            }
        }

        throw new CustomException(ErrorType.VALIDATION_ERROR, "Un Supported Enum Type");
    }
}