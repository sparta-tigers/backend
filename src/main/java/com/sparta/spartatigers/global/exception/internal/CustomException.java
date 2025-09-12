package com.sparta.spartatigers.global.exception.internal;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final ExceptionCode exceptionCode;
	private final Object data;

	public CustomException(ExceptionCode exceptionCode) {
		super(exceptionCode.getMessage());
		this.exceptionCode = exceptionCode;
		this.data = null;
	}

	public CustomException(ExceptionCode exceptionCode, Object data) {
		super(exceptionCode.getMessage());
		this.exceptionCode = exceptionCode;
		this.data = data;
	}
}
