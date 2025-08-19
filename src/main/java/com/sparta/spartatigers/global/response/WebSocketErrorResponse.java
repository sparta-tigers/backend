package com.sparta.spartatigers.global.response;

import com.sparta.spartatigers.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class WebSocketErrorResponse {

    private final String errorType;
    private final String message;

    public static WebSocketErrorResponse from(ExceptionCode code) {
        return WebSocketErrorResponse.of(code.getStatus().name(), code.getMessage());
    }
}
