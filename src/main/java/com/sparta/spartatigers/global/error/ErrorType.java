package com.sparta.spartatigers.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // 아이템
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "아이템을 찾을 수 없습니다.", LogLevel.DEBUG),
    CANNOT_REQUEST_OWN_ITEM(HttpStatus.BAD_REQUEST, ErrorCode.E400, "자신의 아이템에 대해 교환 요청을 할 수 없습니다.", LogLevel.DEBUG),
    RECEIVER_NOT_OWNER(HttpStatus.FORBIDDEN, ErrorCode.E403, "해당 아이템의 소유자에게만 요청을 보낼 수 있습니다.", LogLevel.DEBUG),
    ITEM_FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "아이템의 소유자가 아닙니다.", LogLevel.DEBUG),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "요청값 검증에 실패 했습니다", LogLevel.DEBUG),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "인증된 사용자만 수행할 수 있는 요청입니다.", LogLevel.DEBUG),
    AUTHORIZATION_ERROR(HttpStatus.FORBIDDEN, ErrorCode.E403, "권한이 부족한 유저입니다.", LogLevel.DEBUG),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "예상하지 못한 예외가 발생 했습니다.", LogLevel.ERROR);


    private final HttpStatus httpStatus;
    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;
}
