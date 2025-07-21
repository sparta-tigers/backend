package com.sparta.spartatigers.global.response;

import com.sparta.spartatigers.global.error.ErrorMessage;
import com.sparta.spartatigers.global.error.ErrorType;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final ResultType result;

    private final T data;

    private final ErrorMessage error;

    public ApiResponse(ResultType result, T data, ErrorMessage error) {
        this.result = result;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<?> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error));
    }

    public static ApiResponse<?> error(ErrorType error, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error, errorData));
    }
}
