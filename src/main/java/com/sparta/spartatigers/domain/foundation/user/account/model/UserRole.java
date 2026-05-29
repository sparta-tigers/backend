package com.sparta.spartatigers.domain.foundation.user.account.model;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    public static UserRole from(String value) {
        for (UserRole role: values()) {
            if (role.toString().equals(value.toUpperCase())) {
                return role;
            }
        }

        throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
    }
}