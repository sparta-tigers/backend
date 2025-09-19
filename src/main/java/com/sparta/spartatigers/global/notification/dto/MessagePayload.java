package com.sparta.spartatigers.global.notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessagePayload {

    private final String subject;
    private final String message;
}
