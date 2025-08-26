package com.sparta.spartatigers.global.aop;

import com.sparta.spartatigers.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {
    //
    // @ExceptionHandler(CustomException.class)
    // public ResponseEntity<ApiResponse<?>> handleChatCustomException(CustomException exception) {
    //     switch (exception.getErrorType().getLogLevel()) {
    //         case ERROR -> log.error("ChatCustomException : {}", exception.getMessage(), exception);
    //         case WARN -> log.warn("ChatCustomException : {}", exception.getMessage(), exception);
    //         default -> log.info("ChatCustomException : {}", exception.getMessage(), exception);
    //     }
    //
    //     return new ResponseEntity<>(
    //             ApiResponse.error(exception.getErrorType(), exception.getData()),
    //             exception.getErrorType().getHttpStatus());
    // }
    //
    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
    //         MethodArgumentNotValidException exception) {
    //     // 검증 실패 항목을 Map의 key-value 형식으로 수집
    //     Map<String, String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
    //             .collect(Collectors.groupingBy(
    //                     FieldError::getField, // 필드 이름(key)
    //                     Collectors.mapping(
    //                             DefaultMessageSourceResolvable::getDefaultMessage,
    //                             Collectors.joining(", ")) // 해당 검증 오류 메시지들을 콤마로 구분하여 연결
    //                     ));
    //
    //     return new ResponseEntity<>(
    //             ApiResponse.error(ErrorType.VALIDATION_ERROR, validationErrors),
    //             ErrorType.VALIDATION_ERROR.getHttpStatus());
    // }
    //
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
    //     log.error("Exception : {}", exception.getMessage(), exception);
    //
    //     return new ResponseEntity<>(
    //             ApiResponse.error(ErrorType.INTERNAL_SERVER_ERROR), ErrorType.INTERNAL_SERVER_ERROR.getHttpStatus());
    // }
}
