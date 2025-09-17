package com.sparta.spartatigers.domain.user.service;

import com.sparta.spartatigers.domain.auth.service.PasswordEncoder;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User addUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new InvalidRequestException(ExceptionCode.EMAIL_ALREADY_USED);
        }

        String hashedPassword = passwordEncoder.hash(user.getPassword());
        user.changePassword(hashedPassword);

        return userRepository.save(user);
    }
}
