package com.sparta.spartatigers.domain.user.repository;

import com.sparta.spartatigers.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u.nickname FROM users u WHERE u.id = :userId")
    Optional<String> findNicknameById(Long userId);

}
