package com.sparta.spartatigers.domain.support.chat.controller;

import com.sparta.spartatigers.domain.support.chat.service.LocationService;
import com.sparta.spartatigers.domain.support.chat.dto.request.LocationRequestDto;
import com.sparta.spartatigers.domain.support.chat.interceptor.StompPrincipal;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // @MessageMapping("/location.update")
    // public void updateLocation(@Payload LocationRequestDto request, Principal
    // principal) {
    // Long userId;
    //
    // if (principal instanceof StompPrincipal stompPrincipal) {
    // userId = Long.parseLong(stompPrincipal.getName());
    // } else {
    // throw new IllegalStateException("지원하지 않는 principal 타입");
    // }
    //
    // locationService.updateLocation(request, userId);
    // }
    @MessageMapping("/location.update")
    public void updateLocation(@Payload LocationRequestDto request, Principal principal) {
        Long userId = Long.parseLong(((StompPrincipal) principal).getName());
        locationService.updateLocation(request, userId);
    }
}
