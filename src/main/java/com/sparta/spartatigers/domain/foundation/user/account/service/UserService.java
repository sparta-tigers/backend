package com.sparta.spartatigers.domain.foundation.user.account.service;

import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.global.firebase.dto.FcmTokenRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.foundation.user.auth.service.PasswordEncoder;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.domain.foundation.user.account.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Transactional
    public void updateFcmToken(TokenClaim tokenClaim, FcmTokenRequest request) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));

        user.updateFcmToken(request.fcmToken());
    }
}
