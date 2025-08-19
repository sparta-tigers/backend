package com.sparta.spartatigers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class InvalidRequestException extends BaseException {

    private final ExceptionCode exceptionCode;

    @Override
    public HttpStatus getStatus() {
        return exceptionCode.getStatus();
    }

    @Override
    public String getMessage() {
        return exceptionCode.getMessage();
    }
}
