package com.sparta.spartatigers.domain.foundation.user.auth.service;

public interface PasswordEncoder {
    String hash(String password);

    boolean matches(String password, String hashed);
}
