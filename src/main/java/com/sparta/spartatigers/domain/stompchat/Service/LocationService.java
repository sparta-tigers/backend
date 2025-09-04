package com.sparta.spartatigers.domain.stompchat.Service;

import com.sparta.spartatigers.domain.stompchat.dto.request.LocationRequestDto;
import com.sparta.spartatigers.domain.stompchat.dto.response.LocationUpdateDto;
import com.sparta.spartatigers.domain.team.model.Stadium;
import com.sparta.spartatigers.domain.team.repository.StadiumRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private static final String USER_LOCATION_KEY = "user:locations";
    private static final String STADIUM_CHANNEL_KEY = "location:stadium:";
    private static final String STADIUM_LOCATION_KEY = "stadiums:";
    private static final double NEAR_STADIUM_KM = 1.0;
    private static final double CLOSEST_STADIUM_KM = 100;
    private final RedisTemplate<String, String> redisTemplate;
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
                redisTemplate.opsForGeo().add(STADIUM_LOCATION_KEY, point,
                    String.valueOf(stadium.getId()));
            }
            log.info("[loadStadiumLocation] 야구장 위치 로딩 완료: {}개", stadiums.size());
        } catch (Exception e) {
            log.error("[loadStadiumLocation] 야구장 위치 로딩 중 예외 발생", e);
        }
    }

    public void updateLocation(LocationRequestDto request, Long userId) {
        if (userId == null) {
            return;
        }

        Point point = new Point(request.getLongitude(), request.getLatitude());
        redisTemplate.opsForGeo().add(USER_LOCATION_KEY, point, String.valueOf(userId));

        Long stadiumId = findClosestStadiumId(request.getLatitude(), request.getLongitude());
        LocationUpdateDto locationDto = LocationUpdateDto.of(userId, request, stadiumId);

        String redisChannel = STADIUM_CHANNEL_KEY + stadiumId;
        redisTemplate.convertAndSend(redisChannel, locationDto);
    }

    private Long findClosestStadiumId(double latitude, double longitude) {

        Point point = new Point(longitude, latitude);
        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
            .includeDistance()
            .sortAscending()
            .limit(1);
        Distance radius = new Distance(CLOSEST_STADIUM_KM, Metrics.KILOMETERS);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
            redisTemplate.opsForGeo().search(STADIUM_LOCATION_KEY, GeoReference.fromCoordinate(point), GeoShape.byRadius(radius), args);

        if (results == null || results.getContent().isEmpty()) {
            return null;
        }

        String stadium = results.getContent().getFirst().getContent().getName();
        return Long.valueOf(stadium);
    }

    public boolean isNearStadium(double longitude, double latitude) {
        try {
            Point point = new Point(longitude, latitude);
            Distance distance = new Distance(NEAR_STADIUM_KM, Metrics.KILOMETERS);
            Circle circle = new Circle(point, distance);
            GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(STADIUM_LOCATION_KEY, circle);

            return results != null && !results.getContent().isEmpty();
        } catch (Exception e) {
            log.error("[isNearStadium] 야구장 인근 확인 중 예외 발생", e);
            return false;
        }
    }
}
