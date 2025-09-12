package com.sparta.spartatigers.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final ResultType resultType;
    private final T data;
    private final ErrorResponse error;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private ApiResponse(ResultType resultType, T data, ErrorResponse error) {
        this.resultType = resultType;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(final T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> created(final T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<Object> error(final ExceptionCode code) {
        return new ApiResponse<>(ResultType.ERROR, null, ErrorResponse.of(code));
    }

    public static ApiResponse<Object> error(final ExceptionCode code, final Object data) {
        return new ApiResponse<>(ResultType.ERROR, null, ErrorResponse.of(code, data));
    }
}
