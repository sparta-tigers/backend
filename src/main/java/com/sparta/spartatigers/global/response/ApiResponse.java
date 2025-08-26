package com.sparta.spartatigers.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.spartatigers.global.exception.BaseException;
import com.sparta.spartatigers.global.exception.ExceptionCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final ResultType resultType;
    private final T data;
    private final ErrorResponse error;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private ApiResponse(ResultType resultType, T data, ErrorResponse error) {
        this.resultType=resultType;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(final T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> ok(final T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> created(final T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<Object> fail(final BaseException ex) {
        return new ApiResponse<>(
            ResultType.ERROR, null, ErrorResponse.of(ex.getExceptionCode()));
    }

    public static ApiResponse<Object> fail(
            ExceptionCode code, List<ErrorResponse.FieldErrorDetail> fieldErrors) {
        return new ApiResponse<>(
                ResultType.ERROR, null, ErrorResponse.of(code, fieldErrors));
    }

    public static ApiResponse<?> error(ExceptionCode error) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorResponse(error));
    }

    public static ApiResponse<?> error(ExceptionCode error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorResponse(error, errorData));
    }

}
