package com.sparta.spartatigers.global.aop;

import com.sparta.spartatigers.domain.foundation.user.account.model.User;
import com.sparta.spartatigers.domain.foundation.user.account.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenClaim {

    private String subject;
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private UserRole role;

    public static TokenClaim from(User user) {
        return builder()
            .subject(user.getId().toString())
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .role(user.getRole())
            .build();
    }
}
