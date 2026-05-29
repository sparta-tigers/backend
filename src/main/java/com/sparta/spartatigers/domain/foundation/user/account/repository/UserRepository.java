package com.sparta.spartatigers.domain.foundation.user.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u.nickname FROM users u WHERE u.id = :userId")
    Optional<String> findNicknameById(Long userId);

    default User findByIdOrElseThrow(Long userId) {
        return findById(userId)
            .orElseThrow(()-> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
    }

}
