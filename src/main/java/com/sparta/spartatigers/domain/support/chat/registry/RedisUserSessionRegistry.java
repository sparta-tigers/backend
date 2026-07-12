package com.sparta.spartatigers.domain.support.chat.registry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUserSessionRegistry {

    private static final String USER_SESSION_KEY_PREFIX = "user-sessions:"; // userId -> Set<sessionId>
    private static final String ROOM_USERS_KEY_PREFIX = "room-users:";
    private final StringRedisTemplate redisTemplate;

    // 멀티 세션 불가 x (메시지 중복 수신 문제 발생)
    // TODO: 추후에 멀티 디바이스 환경에도 사용할 수 있게 하려면 멀티 세션이 가능하게 대응
    // 지금은 기존 세션을 전부 제거하고 있기 때문에 단일 세션의 효과
    public void registerSession(Long userId, String sessionId) {
        String userKey = USER_SESSION_KEY_PREFIX + userId;

        // 기존 세션 제거
        Set<String> existingSessions = getSessionIds(userId);
        if (existingSessions != null) {
            for (String oldSessionId : existingSessions) {
                redisTemplate.opsForSet().remove(userKey, oldSessionId);
                redisTemplate.delete("session-user:" + oldSessionId);
            }
        }

        // 새 세션 등록
        // userId별로 세션ID를 Set에 추가
        redisTemplate.opsForSet().add(userKey, sessionId);
        redisTemplate.expire(userKey, Duration.ofHours(6));
        // 단일 키 구조로 변경
        String sessionKey = "session-user:" + sessionId;
        redisTemplate
            .opsForValue()
            .set(sessionKey, userId.toString(), Duration.ofHours(6));
    }

    public void unregisterSession(Long userId, String sessionId) {
        String userKey = USER_SESSION_KEY_PREFIX + userId;

        // Set에서 세션ID 제거
        redisTemplate.opsForSet().remove(userKey, sessionId);
        // 세션ID 제거
        redisTemplate.delete("session-user:" + sessionId);

        // 세션이 모두 제거되면 키 자체 제거
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().size(userKey) == 0)) {
            redisTemplate.delete(userKey);
        }
    }

    public boolean isUserConnected(Long userId) {
        String userKey = USER_SESSION_KEY_PREFIX + userId;
        // userKey가 Redis에 존재하는지 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
    }

    public Map<Long, Boolean> areUsersConnected(List<Long> userIds) {
        Map<Long, Boolean> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return result;

        // [FIX] N+1 I/O 방지를 위해 Redis Pipeline 사용
        List<Object> exists = redisTemplate.executePipelined(
            (org.springframework.data.redis.connection.RedisConnection connection) -> {
                org.springframework.data.redis.connection.StringRedisConnection stringConn =
                    (org.springframework.data.redis.connection.StringRedisConnection) connection;
                for (Long userId : userIds) {
                    stringConn.exists(USER_SESSION_KEY_PREFIX + userId);
                }
                return null;
            }
        );

        for (int i = 0; i < userIds.size(); i++) {
            // executePipelined 결과는 요청 순서와 동일함
            result.put(userIds.get(i), Boolean.TRUE.equals(exists.get(i)));
        }

        return result;
    }

    public Long getUserIdBySessionId(String sessionId) {
        String sessionKey = "session-user:" + sessionId;
        String userId = redisTemplate.opsForValue().get(sessionKey);
        return userId != null ? Long.parseLong(userId) : null;
    }

    public Set<String> getSessionIds(Long userId) {
        String userKey = USER_SESSION_KEY_PREFIX + userId;
        return redisTemplate.opsForSet().members(userKey);
    }

    // --------------------------------------------------------------------
    // 유저 입장시
    public void registerUserInRoom(Long roomId, Long userId) {
        String key = ROOM_USERS_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
        redisTemplate.expire(key, Duration.ofHours(6)); // TODO: 일단 태정님하고 똑같이 6시간,,,
    }

    // 유저 퇴장시
    public void unregisterUserInRoom(Long roomId, Long userId) {
        String key = ROOM_USERS_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, String.valueOf(userId));
    }

    // 방에 유저 있는지 확인
    public boolean isUserInRoom(Long roomId, Long userId) {
        String key = ROOM_USERS_KEY_PREFIX + roomId;
        return Boolean.TRUE.equals(
            redisTemplate.opsForSet().isMember(key, String.valueOf(userId))
        );
    }
}
