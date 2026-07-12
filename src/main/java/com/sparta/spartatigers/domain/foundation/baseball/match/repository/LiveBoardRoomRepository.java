package com.sparta.spartatigers.domain.foundation.baseball.match.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardRoom;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LiveBoardRoomRepository {

    private static final String LIVEBOARD_ROOMS = "liveboard:rooms";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private HashOperations<String, String, LiveBoardRoom> opsHash;

    @PostConstruct
    private void init() {
        opsHash = redisTemplate.opsForHash();
    }

    public void saveRoom(LiveBoardRoom room) {
        opsHash.put(LIVEBOARD_ROOMS, room.getRoomId(), room);
    }

    public LiveBoardRoom findRoomById(String roomId) {
        Object object = opsHash.get(LIVEBOARD_ROOMS, roomId);
        return objectMapper.convertValue(object, LiveBoardRoom.class);
    }

    public List<LiveBoardRoom> findAllByDate(LocalDate date) {
        return findAllRoom()
            .stream()
            .filter(room -> room.getMatchTime().toLocalDate().equals(date))
            .toList();
    }

    // 매치아이디로 라이브보드룸찾기
    public List<LiveBoardRoom> findAllByMatchIdIn(Set<Long> matchIds) {
        List<LiveBoardRoom> all = findAllRoom();

        return all
            .stream()
            .filter(room -> matchIds.contains(room.getMatchId()))
            .toList();
    }

    public List<LiveBoardRoom> findAllRoom() {
        List<LiveBoardRoom> rooms = new ArrayList<>();
        for (Object room : opsHash.values(LIVEBOARD_ROOMS)) {
            rooms.add(objectMapper.convertValue(room, LiveBoardRoom.class));
        }
        return rooms;
    }

    public void deleteRoom(String roomId) {
        opsHash.delete(LIVEBOARD_ROOMS, roomId);
    }

    public boolean existsById(String roomId) {
        return opsHash.hasKey(LIVEBOARD_ROOMS, roomId);
    }
}
