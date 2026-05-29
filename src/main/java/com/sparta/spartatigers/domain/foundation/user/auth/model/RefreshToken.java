package com.sparta.spartatigers.domain.foundation.user.auth.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String token;

    private String subject;

    @TimeToLive
    private long ttlSeconds;

    @Builder
    public RefreshToken(String token, String subject, long ttlSeconds) {
        this.token = token;
        this.subject = subject;
        this.ttlSeconds = ttlSeconds;
    }
}
