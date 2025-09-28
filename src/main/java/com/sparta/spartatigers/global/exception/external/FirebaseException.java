package com.sparta.spartatigers.global.exception.external;

import org.springframework.http.HttpStatus;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FirebaseException extends ExternalServiceException {

    private final ExceptionCode exceptionCode;

    public FirebaseException(ExceptionCode code, Throwable cause) {
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

    @Override
    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    @Override
    public String getSource() {
        return "Firebase";
    }
}