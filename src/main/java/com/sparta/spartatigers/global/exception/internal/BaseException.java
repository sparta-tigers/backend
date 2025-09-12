package com.sparta.spartatigers.global.exception.internal;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    public abstract HttpStatus getStatus();

    public abstract String getMessage();

    public abstract ExceptionCode getExceptionCode();
}
