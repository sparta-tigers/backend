package com.sparta.spartatigers.global.response;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class WebSocketErrorResponse {

    private final String code;
    private final String message;

    public static WebSocketErrorResponse from(ExceptionCode code) {
        return WebSocketErrorResponse.of(
            code.getCode().name(),
            code.getMessage()
        );
    }
}
