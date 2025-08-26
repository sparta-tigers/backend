package com.sparta.spartatigers.domain.user.model;

import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.InvalidRequestException;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    public static UserRole from(String value) {
        for (UserRole role: values()) {
            if (role.toString().equals(value.toUpperCase())) {
                return role;
            }
        }

        throw new InvalidRequestException(ExceptionCode.NOT_VALID_EXCEPTION);
    }
}