package com.sparta.spartatigers.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final Object data;

    private ErrorResponse(final ExceptionCode code, final Object data) {
        this.code = code.getCode().name();
        this.message = code.getMessage();
        this.data = data;
    }

    public static ErrorResponse of(final ExceptionCode code) {
        return new ErrorResponse(code, null);
    }

    public static ErrorResponse of(final ExceptionCode code, final Object data) {
        return new ErrorResponse(code, data);
    }

    /**
     * @Valid 검증 실패 시 발생하는 필드 에러 정보를 담는 내부 클래스
     */
    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class FieldErrorDetail {

        private String field;
        private Object rejectedValue;
        private String reason;
    }
}
