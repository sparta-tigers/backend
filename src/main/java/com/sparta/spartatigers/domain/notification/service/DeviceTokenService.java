package com.sparta.spartatigers.domain.notification.service;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.notification.dto.DeviceTokenRequest;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final UserRepository userRepository;

    @Transactional
    public void registerDeviceToken(TokenClaim tokenClaim, DeviceTokenRequest request) {
        User user = userRepository.findById(tokenClaim.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateDeviceToken(request.token());
        
        log.info(
                "[DeviceToken] userId={}, deviceType={}, tokenLength={} - DB 저장 완료",
                tokenClaim.getUserId(),
                request.deviceType(),
                request.token().length()
        );
    }
}
