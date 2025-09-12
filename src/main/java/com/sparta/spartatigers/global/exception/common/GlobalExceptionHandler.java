package com.sparta.spartatigers.global.exception.common;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.external.ExternalServiceException;
import com.sparta.spartatigers.global.exception.external.FirebaseException;
import com.sparta.spartatigers.global.exception.internal.BaseException;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.exception.internal.ServerException;
import com.sparta.spartatigers.global.response.ApiResponse;
import com.sparta.spartatigers.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 구체적인 예외
     */
    // validation 예외 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("Validation 예외 발생: {}", ex.getMessage());

        List<ErrorResponse.FieldErrorDetail> fieldErrorDetails =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        ErrorResponse.FieldErrorDetail.of(
                                                error.getField(),
                                                error.getRejectedValue(),
                                                error.getDefaultMessage()))
                        .toList();

        ApiResponse<?> response =
                ApiResponse.fail(ExceptionCode.VALIDATION_ERROR, fieldErrorDetails);
        return ResponseEntity.status(ExceptionCode.VALIDATION_ERROR.getHttpStatus()).body(response);
    }

    // Valid에서 못거르는 타입 불일치 메소외드 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFormat(HttpMessageNotReadableException ex) {
        log.warn("요청 데이터 형식 오류: {}", ex.getMessage());

        return ResponseEntity.badRequest()
            .body(ApiResponse.fail(ExceptionCode.INVALID_TYPE_EXCEPTION));
    }

    /**
     * 서비스 계층 예외 (외부 -> 내부)
     */
    // 외부 예외 핸들러
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleExternalServiceException(ExternalServiceException ex) {
        log.error("Firebase 예외 발생", ex);

        // TODO: 디코 등 운영팀 알림 로직 추가

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.fail(ex.getExceptionCode()));
    }

    // 내부 예외 핸들러
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {
        log.warn("내부 비즈니스 로직 예외 발생: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.fail(ex.getExceptionCode()));
    }

    /**
     * SSE 관련 예외
     */
    /*
    SSE는 클라 동작 없으면 연결 끊김 -> 끊김 시 글로벌 익셉션 핸들러를 타는데 얘는 JSON 예외만 뱉기에 처리 못해서 생성
    예외가 발생해도 재연결을 시도하기에 동작에는 지장없음 해당 예외는 예외 로그가 계성속 생기기에 생성
    */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleSseWritableException(HttpServletRequest request, Exception e) {
        if (request.getRequestURI().contains("/sse/subscribe")) {
            log.debug("SSE 응답 처리 중 예외 발생 (무시 가능): {}", e.getMessage());
        } else {
            log.warn("HttpMessageNotWritableException 발생: ", e);
        }
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeoutException() {
        log.info("SSE 재연결 중 ");
    }

    /**
     * 최후의 보루
     */
    // 예상치 못한 예외 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage());
        ServerException serverException = new ServerException(ExceptionCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(serverException.getStatus())
            .body(ApiResponse.fail(serverException.getExceptionCode()));
    }
}
