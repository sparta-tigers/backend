package com.sparta.spartatigers.global.exception;

public class WebSocketException extends RuntimeException {

    private final ExceptionCode code;

    public WebSocketException(ExceptionCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public ExceptionCode getCode() {
        return code;
    }
}
