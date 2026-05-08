package com.sparta.spartatigers.global.exception.common;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import jakarta.validation.ConstraintViolationException;

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.external.ExternalServiceException;
import com.sparta.spartatigers.global.exception.internal.BaseException;
import com.sparta.spartatigers.global.notification.NotificationSender;
import com.sparta.spartatigers.global.notification.dto.AlertLevel;
import com.sparta.spartatigers.global.notification.dto.MessagePayload;
import com.sparta.spartatigers.global.response.ApiResponse;
import com.sparta.spartatigers.global.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final NotificationSender notificationSender;

    // 민감 정보 필드명 관리하는 Set
    private static final Set<String> SENSITIVE_FIELDS =
        Set.of("password", "pwd", "pass", "token", "authorization", "auth", "secret", "apiKey", "api_key");

    /**
     * 값 마스킹 헬퍼 메서드
     */
    private static Object maskIfSensitive(String field, Object value) {
        String fieldName = (field != null) ? field.toLowerCase() : "";

        // Set에 포함된 키워드 중 하나라도 필드명에 포함되면 마스킹 처리
        if (SENSITIVE_FIELDS.stream().anyMatch(fieldName::contains)) {
            return "******";
        }
        return value;
    }

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
                            maskIfSensitive(error.getField(), error.getRejectedValue()),
                            error.getDefaultMessage()))
                .toList();

        ApiResponse<?> response =
            ApiResponse.error(ExceptionCode.VALIDATION_ERROR, fieldErrorDetails);
        return ResponseEntity.status(ExceptionCode.VALIDATION_ERROR.getHttpStatus()).body(response);
    }

    /**
     * ConstraintViolationException 핸들러 (Query Parameter 검증 실패 시 발생)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("ConstraintViolation 예외 발생: {}", ex.getMessage());

        List<ErrorResponse.FieldErrorDetail> fieldErrorDetails = ex.getConstraintViolations().stream()
            .map(violation -> {
                String propertyPath = violation.getPropertyPath().toString();
                String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                return ErrorResponse.FieldErrorDetail.of(
                    fieldName,
                    maskIfSensitive(fieldName, violation.getInvalidValue()),
                    violation.getMessage()
                );
            })
            .toList();

        ApiResponse<?> response = ApiResponse.error(ExceptionCode.VALIDATION_ERROR, fieldErrorDetails);
        return ResponseEntity.status(ExceptionCode.VALIDATION_ERROR.getHttpStatus()).body(response);
    }

    // Valid에서 못거르는 타입 불일치 메소외드 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFormat(HttpMessageNotReadableException ex) {
        log.warn("요청 데이터 형식 오류: {}", ex.getMessage());

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ExceptionCode.INVALID_TYPE_EXCEPTION));
    }

    // [FIX] 문제 2: check-then-act 경합으로 DataIntegrityViolationException 발생 시 
    // 무조건 ITEM_ALREADY_EXISTS 매핑하던 것을 UK_ACTIVE_ITEM_PER_USER 제약조건 위반인 경우에 한해 409로 분기. 
    // 그 외(NOT NULL, 길이 초과 등)는 500 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String rootMessage = ex.getMostSpecificCause() != null
            ? String.valueOf(ex.getMostSpecificCause().getMessage())
            : "";
        log.warn("DB 무결성 제약 위반: {}", rootMessage);

        // UK_ACTIVE_ITEM_PER_USER 위반에 한해 ITEM_ALREADY_EXISTS로 매핑
        if (rootMessage != null && rootMessage.toUpperCase().contains("UK_ACTIVE_ITEM_PER_USER")) {
            return ResponseEntity.status(ExceptionCode.ITEM_ALREADY_EXISTS.getHttpStatus())
                .body(ApiResponse.error(ExceptionCode.ITEM_ALREADY_EXISTS));
        }

        // 그 외 무결성 위반은 일반 처리
        return ResponseEntity.status(ExceptionCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(ExceptionCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 서비스 계층 예외 (외부 -> 내부)
     */
    // 외부 예외 핸들러
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleExternalServiceException(ExternalServiceException ex) {
        // String errorSource = ex.getSource(); // 임시 비활성화
        // String title = String.format("외부 서비스(%s) 오류 발생", errorSource); // 임시 비활성화

        // sendNotificationToDiscord(AlertLevel.CRITICAL, title, ex); // 임시 비활성화

        return ResponseEntity.status(ex.getStatus())
            .body(ApiResponse.error(ex.getExceptionCode()));
    }

    // 내부 예외 핸들러
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {
        log.warn("내부 비즈니스 로직 예외 발생: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getExceptionCode()));
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
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        // String title = "처리하지 못한 내부 비즈니즈 로직 오류 발생"; // 임시 비활성화

        // sendNotificationToDiscord(AlertLevel.ERROR, title, ex); // 임시 비활성화

        return ResponseEntity.status(ExceptionCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(ExceptionCode.INTERNAL_SERVER_ERROR));
    }

    private void sendNotificationToDiscord(AlertLevel level, String title, Exception ex) {
        log.error("{} [Alert]: {}", title, ex.getMessage(), ex);

        // BaseException 또는 ExternalServiceException에서 ExceptionCode를 가져오기 위한 처리
        ExceptionCode code = null;
        if (ex instanceof BaseException) {
            code = ((BaseException) ex).getExceptionCode();
        } else if (ex instanceof ExternalServiceException) {
            code = ((ExternalServiceException) ex).getExceptionCode();
        }

        MessagePayload payload = MessagePayload.builder()
            .level(level)
            .subject(title)
            .message(ex.getMessage())
            .metadata(Map.of(
                "Exception Type", ex.getClass().getSimpleName(),
                "Caused By", ex.getCause() != null ? ex.getCause().getMessage() : "원인 정보 없음",
                "Error Code", (code != null) ? code.getCode().name() : "에러 코드 없음",
                "Timestamp", LocalDateTime.now().toString()
            ))
            .build();

        notificationSender.send(payload);
    }
}
