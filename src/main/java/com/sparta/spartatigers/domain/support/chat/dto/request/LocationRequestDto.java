package com.sparta.spartatigers.domain.support.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDto {

    private double latitude;
    private double longitude;
}
