package com.sparta.spartatigers.global.response;

import com.sparta.spartatigers.global.exception.BaseException;
import com.sparta.spartatigers.global.exception.ErrorCode;
import com.sparta.spartatigers.global.exception.ExceptionCode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ErrorResponse {

    private String code;
    private String message;
    private Object data;
    private List<FieldErrorDetail> fieldErrors;

    public ErrorResponse(ExceptionCode code) {
        this.code = code.getCode().name();
        this.message = code.getMessage();
        this.data = null;
    }

    public ErrorResponse(ExceptionCode code, Object data) {
        this.code = code.getCode().name();
        this.message = code.getMessage();
        this.data = data;
    }

    public static ErrorResponse of(ExceptionCode code) {
        return new ErrorResponse(code);
    }


    public ErrorResponse(ExceptionCode code, List<FieldErrorDetail> fieldErrors) {
        this.message = code.getMessage();
        this.fieldErrors = fieldErrors;
    }


    public static ErrorResponse of(ExceptionCode code, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(code, fieldErrors);
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class FieldErrorDetail {

        private String field;
        private Object rejectedValue;
        private String reason;
    }
}
