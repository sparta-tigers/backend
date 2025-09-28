package com.sparta.spartatigers.global.util;

import java.time.Duration;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiter {

    private static final String RATE_LIMIT_LUA_SCRIPT =
            // 1을 증가시키고
            "local current = redis.call('incr', KEYS[1]) "
                    +
                    // 값이 1이면 TTL 설정
                    "if current == 1 then redis.call('expire', KEYS[1], ARGV[1]) end "
                    + "return current";
    private final StringRedisTemplate redisTemplate;

    /**
     * redis를 이용해 사용자의 요청 횟수를 제한하는 기능
     *
     * @param key redis에 저장할 키
     * @param limit 허용 횟수
     * @param limitTime 제한 시간
     * @return 요청 횟수가 limit보다 커지면 차단됨
     */
    public boolean isRateLimited(String key, int limit, Duration limitTime) {
        try {
            Long count =
                    redisTemplate.execute(
                            (RedisCallback<Long>)
                                    connection ->
                                            connection.eval(
                                                    RATE_LIMIT_LUA_SCRIPT.getBytes(),
                                                    ReturnType.INTEGER,
                                                    1,
                                                    key.getBytes(),
                                                    String.valueOf(limitTime.getSeconds())
                                                            .getBytes()));
            return count != null && count > limit;
        } catch (DataAccessException e) {
            log.warn("redis 장애 발생 채팅 제한하지 않음");
            return false;
        }
    }
}
