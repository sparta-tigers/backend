package com.sparta.spartatigers.domain.liveboard.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.sparta.spartatigers.domain.liveboard.model.LiveBoardConnection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class LiveBoardConnectionRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String PREFIX = "liveboard:connections:";

	public void saveConnection(String roomId, String sessionId, LiveBoardConnection connection) {
		redisTemplate.opsForHash().put(PREFIX + roomId, sessionId, connection);
	}

	public void deleteConnection(String roomId, String sessionId) {
		redisTemplate.opsForHash().delete(PREFIX + roomId, sessionId);
	}

	public void deleteAllConnections(String roomId) {
		redisTemplate.delete(PREFIX + roomId);
	}

	public Long getConnectionCount(String roomId) {
		return redisTemplate.opsForHash().size(PREFIX + roomId);
	}

	public List<String> findAllRoomIds() {
		Set<String> keys = redisTemplate.keys(PREFIX + "*");
		return keys.stream().map(key -> key.replace(PREFIX, "")).collect(Collectors.toList());
	}

	public Map<Object, Object> findAllConnections(String roomId) {
		return redisTemplate.opsForHash().entries(PREFIX + roomId);
	}
}
