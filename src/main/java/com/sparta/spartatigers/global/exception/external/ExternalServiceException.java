package com.sparta.spartatigers.global.exception.external;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;

public abstract class ExternalServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ExternalServiceException() {
        super();
    }

    protected ExternalServiceException(String message) {
        super(message);
    }

    protected ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ExternalServiceException(Throwable cause) {
        super(cause);
    }

    public abstract HttpStatus getStatus();

    public abstract String getMessage();

    public abstract ExceptionCode getExceptionCode();

    public abstract String getSource();
}
