package com.sparta.spartatigers.global.exception.internal;

import org.springframework.http.HttpStatus;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidRequestException extends BaseException {

    private final ExceptionCode exceptionCode;

    public InvalidRequestException(ExceptionCode code, Throwable cause) {
        super(cause);
        this.exceptionCode = code;
    }

    @Override
    public HttpStatus getStatus() {
        return exceptionCode.getHttpStatus();
    }

    @Override
    public String getMessage() {
        return exceptionCode.getMessage();
    }
}
