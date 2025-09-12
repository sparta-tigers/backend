package com.sparta.spartatigers.global.exception.external;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import org.springframework.http.HttpStatus;

public abstract class ExternalServiceException extends RuntimeException {

    public abstract HttpStatus getStatus();

    public abstract String getMessage();

    public abstract ExceptionCode getExceptionCode();
}
