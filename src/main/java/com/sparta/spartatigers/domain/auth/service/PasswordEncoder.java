package com.sparta.spartatigers.domain.auth.service;

public interface PasswordEncoder {
    String hash(String password);

    boolean matches(String password, String hashed);
}
