package com.sparta.spartatigers.domain.user.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@EqualsAndHashCode(of = "userId")
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LoginUser from(User user) {
        return builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
