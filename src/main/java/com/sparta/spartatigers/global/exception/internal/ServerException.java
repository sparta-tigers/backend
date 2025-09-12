package com.sparta.spartatigers.global.exception.internal;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ServerException extends BaseException {

    private final ExceptionCode exceptionCode;

    @Override
    public HttpStatus getStatus() {
        return exceptionCode.getHttpStatus();
    }

    @Override
    public String getMessage() {
        return exceptionCode.getMessage();
    }
}
