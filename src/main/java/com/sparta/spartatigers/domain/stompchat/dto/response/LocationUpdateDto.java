package com.sparta.spartatigers.domain.stompchat.dto.response;

import com.sparta.spartatigers.domain.stompchat.dto.request.LocationRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {

    private Long userId;
    private double latitude;
    private double longitude;
    private Long stadiumId;

    public static LocationUpdateDto of(Long userId, LocationRequestDto location, Long stadiumId) {
        return new LocationUpdateDto(userId, location.getLatitude(), location.getLongitude(),
            stadiumId);
    }
}
