package com.sparta.spartatigers.global.exception.internal;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;

public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected BaseException() {
        super();
    }

    protected BaseException(String message) {
        super(message);
    }

    protected BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BaseException(Throwable cause) {
        super(cause);
    }

    public abstract HttpStatus getStatus();

    public abstract String getMessage();

    public abstract ExceptionCode getExceptionCode();
}
