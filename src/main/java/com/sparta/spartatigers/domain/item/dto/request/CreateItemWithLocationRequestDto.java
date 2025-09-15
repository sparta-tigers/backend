package com.sparta.spartatigers.domain.item.dto.request;

import com.sparta.spartatigers.domain.stompchat.dto.request.LocationRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemWithLocationRequestDto {

    private CreateItemRequestDto itemDto;
    private LocationRequestDto locationDto;
}
