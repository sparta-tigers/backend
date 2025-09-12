package com.sparta.spartatigers.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BcryptPasswordEncoderTest {

    BcryptPasswordEncoder encoder = new BcryptPasswordEncoder();

    @Test
    @DisplayName("해시된 패스워드는 원본과 다르다")
    void hash() {
        String password = "testPassword";

        String hashed = encoder.hash(password);

        assertThat(password).isNotEqualTo(hashed);
    }

    @Test
    @DisplayName("올바른 패스워드는 해시와 매칭된다")
    void match() {
        String password = "testPassword";
        String hashed = encoder.hash(password);

        boolean result = encoder.matches(password, hashed);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 패스워드는 해시와 매칭되지 않는다.")
    void match_false() {
        String password = "testPassword";
        String wrongPassword = "wrongPassword";
        String hashed = encoder.hash(password);

        boolean result = encoder.matches(wrongPassword, hashed);

        assertThat(result).isFalse();
    }
}