package com.sparta.spartatigers.domain.stompchat.dto.response;

import com.sparta.spartatigers.domain.stompchat.dto.request.LocationRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedisUpdateDto {

    private Long userId;
    private double latitude;
    private double longitude;

    public static RedisUpdateDto of(Long userId, LocationRequestDto location) {
        return new RedisUpdateDto(userId, location.getLatitude(), location.getLongitude());
    }
}
