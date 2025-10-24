package com.sparta.spartatigers.domain.stompchat.Service;
import com.sparta.spartatigers.domain.stompchat.dto.request.LocationRequestDto;
import com.sparta.spartatigers.domain.stompchat.dto.response.RedisUpdateDto;
import com.sparta.spartatigers.domain.stompchat.pubsub.RedisLocationPublisher;
import com.sparta.spartatigers.domain.team.model.Stadium;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sparta.spartatigers.domain.team.repository.StadiumRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private static final String USER_LOCATION_KEY = "USERLOCATION:";
    private static final String LOCATION_TTL_KEY = "USERLOCATION_TTL:";
    private static final String STADIUM_LOCATION_KEY = "STADIUMS:";
    private static final double SEARCH_RADIUS_KM = 0.05;
    private static final double NEAR_STADIUM_KM = 1.0;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLocationPublisher locationPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final StadiumRepository stadiumRepository;

    @PostConstruct
    public void loadStadiumLocation() {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(STADIUM_LOCATION_KEY))) {
                log.info("[loadStadiumLocation] Redis에 이미 야구장 위치 정보가 존재합니다.");
                return;
            }
            List<Stadium> stadiums = stadiumRepository.findAll();

            for (Stadium stadium : stadiums) {
                Point point = new Point(stadium.getLongitude(), stadium.getLatitude());
                redisTemplate.opsForGeo().add(STADIUM_LOCATION_KEY, point, stadium.getId());
                log.debug("[loadStadiumLocation] 야구장 등록 - ID: {}", stadium.getId());
            }
            log.info("[loadStadiumLocation] 야구장 위치 로딩 완료: {}개", stadiums.size());
        } catch (Exception e) {
            log.error("[loadStadiumLocation] 야구장 위치 로딩 중 예외 발생", e);
        }
    }

    public void updateLocation(LocationRequestDto request, Long userId) {
        if (userId == null) {
            log.warn("[updateLocation] userId가 null입니다.");
            return;
        }

        try {
            Point point = new Point(request.getLongitude(), request.getLatitude());
            redisTemplate.opsForGeo().add(USER_LOCATION_KEY, point, userId);
            redisTemplate.opsForValue().set(LOCATION_TTL_KEY + userId, "1", Duration.ofMinutes(2));

            log.debug("[updateLocation] 사용자 위치 업데이트 완료 - userId: {}", userId);
            locationPublisher.publishLocation(RedisUpdateDto.of(userId, request));
        } catch (Exception e) {
            log.error(
                "[updateLocation] Redis 또는 publishLocation 처리 중 예외 발생 - userId: {}", userId, e);
        }
    }

    public List<Long> findUsersNearBy(Long userId, double radius) {
        try {
            Point userPoint = redisTemplate.opsForGeo().position(USER_LOCATION_KEY, userId).get(0);

            if (userPoint == null) {
                log.warn("[findUsersNearBy] 사용자 위치를 찾을 수 없습니다.");
                return new ArrayList<>();
            }
            Distance distance = new Distance(radius, Metrics.KILOMETERS);
            Circle circle = new Circle(userPoint, distance);
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().radius(USER_LOCATION_KEY, circle);

            if (results == null) {
                return new ArrayList<>();
            }
            return results.getContent().stream()
                .map(result -> Long.valueOf(result.getContent().getName().toString()))
                .filter(id -> !id.equals(userId))
                .filter(id -> Boolean.TRUE.equals(redisTemplate.hasKey(LOCATION_TTL_KEY + id)))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[findUsersNearBy] 사용자 위치 조회 중 예외 발생 - userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public void notifyUsersNearBy(Long userId, String messageType, Object data) {
        try {
            List<Long> nearByUserIds = findUsersNearBy(userId, SEARCH_RADIUS_KM);
            nearByUserIds.add(userId);

            Map<String, Object> messagePayload = Map.of("type", messageType, "data", data);

            nearByUserIds.forEach(
                targetUserId -> {
                    String destination = "/server/items/user/" + targetUserId;
                    messagingTemplate.convertAndSend(destination, messagePayload);
                });
            log.debug("[notifyUsersNearBy] 알림 전송 완료");
        } catch (Exception e) {
            log.error("[notifyUsersNearBy] 알람 전송 중 예외 발생 - userId: {}", userId, e);
        }
    }

    public boolean isNearStadium(double longitude, double latitude) {
        try {
            Point point = new Point(longitude, latitude);
            Distance distance = new Distance(NEAR_STADIUM_KM, Metrics.KILOMETERS);
            Circle circle = new Circle(point, distance);
            GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().radius(STADIUM_LOCATION_KEY, circle);

            return results != null && !results.getContent().isEmpty();
        } catch (Exception e) {
            log.error("[isNearStadium] 야구장 인근 확인 중 예외 발생", e);
            return false;
        }
    }
}