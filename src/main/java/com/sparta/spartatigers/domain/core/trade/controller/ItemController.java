package com.sparta.spartatigers.domain.core.trade.controller;

import com.sparta.spartatigers.domain.core.trade.dto.request.FindItemByIdRequestDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.domain.support.image.service.ImageStorageService;
import com.sparta.spartatigers.domain.core.trade.dto.request.ItemCreateRequest;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateItemStatusRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.core.trade.service.ItemService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;
    private final Validator validator; // [FIX] 수동 파싱 후 Bean Validation 명시적 실행용

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createItem(
            @Auth TokenClaim tokenClaim,
            @RequestPart(value = "itemRequest") String itemRequestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            // [FIX] @JsonIgnoreProperties(ignoreUnknown = false)로 오타 필드를 명시적으로 거부
            // + 파싱 후 Validator로 Bean Validation(@NotNull, @NotBlank 등) 명시적 수행
            ItemCreateRequest request = objectMapper.readValue(itemRequestJson, ItemCreateRequest.class);

            // [FIX] 수동 파싱 경로에서 우회되던 Bean Validation을 Validator로 명시적 실행
            Set<ConstraintViolation<ItemCreateRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                log.warn("[createItem] 입력 검증 실패: {}", errorMessages);
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }

            // [FIX] 문제 3: 중복 아이템 검증을 이미지 업로드 이전에 수행하여 불필요한 디스크 I/O 방지
            itemService.validateCanCreateItem(tokenClaim);

            // 1. 추상화된 스토리지에 이미지 저장 (현재는 로컬, 나중엔 S3)
            List<String> storedImageUrls = imageStorageService.uploadImages(images);

            try {
                // 2. 비즈니스 로직 실행 (DTO에 URL 리스트 추가 전달)
                itemService.createItemWithImages(request, tokenClaim, storedImageUrls);
            } catch (Exception e) {
                // 3. 실패 시 업로드된 파일 삭제 (롤백)
                log.error("아이템 생성 실패, 업로드된 파일 삭제: {}", storedImageUrls, e);
                // [FIX] 문제 5: deleteImages()가 예외를 던지면 원래 비즈니스 실패 원인(e)이 가려짐
                // 롤백 보조 동작은 try-catch로 격리하여 원인 예외가 우선 전파되도록 보장
                try {
                    imageStorageService.deleteImages(storedImageUrls);
                } catch (Exception cleanupEx) {
                    log.error("업로드 이미지 정리 실패 (원인 예외와 별개): {}", storedImageUrls, cleanupEx);
                }
                throw e;
            }

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (JsonProcessingException e) {
            log.error("[createItem] JSON 파싱 에러 (알 수 없는 필드 또는 형식 오류): {}", e.getMessage());
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("아이템 생성 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ApiResponse<Page<ReadItemResponseDto>> findAllItems(@Auth TokenClaim tokenClaim,
            @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius) {

        // [FIX] 문제 2: NaN 검증 우회 방지 + radius 단독 전달 거부
        // IEEE 754 규칙: NaN < -90, NaN > 90 모두 false → 기존 범위 검증을 그대로 통과
        // NaN이 Haversine 식에 전달되면 acos(NaN) = NaN → 검색 결과 비거나 예외 없는 오동작
        if (latitude != null || longitude != null) {
            if (latitude == null || longitude == null) {
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }
            // [FIX] isNaN() 선검사 — NaN은 범위 비교에서 항상 false이므로 별도 검사 필수
            if (latitude.isNaN() || latitude < -90 || latitude > 90) {
                log.warn("[findAllItems] 유효하지 않은 위도 값: {}", latitude);
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }
            if (longitude.isNaN() || longitude < -180 || longitude > 180) {
                log.warn("[findAllItems] 유효하지 않은 경도 값: {}", longitude);
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }
            if (radius != null && (radius.isNaN() || radius <= 0)) {
                log.warn("[findAllItems] 유효하지 않은 반경 값: {}", radius);
                throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
            }
        } else if (radius != null) {
            // [FIX] 좌표 없이 radius만 전달된 경우 서비스에서 조용히 무시되던 것을 명시적 거부
            // ?radius=5 단독 전달 시 위치 기반 검색이 작동하지 않음에도 200 반환하는 혼란 방지
            log.warn("[findAllItems] 좌표 없이 radius만 전달됨: {}", radius);
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        }

        Page<ReadItemResponseDto> response = itemService.findAllItems(tokenClaim, pageable, latitude, longitude,
                radius);

        return ApiResponse.success(response);
    }

    @GetMapping("/my")
    public ApiResponse<Page<ReadItemResponseDto>> findMyItems(
            @Auth TokenClaim tokenClaim,
            @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        Page<ReadItemResponseDto> response = itemService.findMyItems(tokenClaim, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/{itemId}")
    public ApiResponse<ReadItemDetailResponseDto> findItemById(@PathVariable Long itemId,
            @Valid @ModelAttribute FindItemByIdRequestDto request) {

        ReadItemDetailResponseDto response = itemService.findItemById(itemId, request);

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<?> deleteItem(@Auth TokenClaim tokenClaim, @PathVariable Long itemId) {

        itemService.deleteItem(tokenClaim, itemId);

        return ApiResponse.success("");
    }

    @PatchMapping("/{itemId}")
    public ApiResponse<ItemResponseDto> updateItem(@Auth TokenClaim tokenClaim,
            @PathVariable Long itemId, @RequestBody UpdateItemRequestDto request) {

        ItemResponseDto response = itemService.updateItem(tokenClaim, itemId, request);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{itemId}/status")
    public ApiResponse<Void> updateItemStatus(
            @Auth TokenClaim tokenClaim,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemStatusRequestDto request) {
        itemService.updateItemStatus(tokenClaim, itemId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/check-active")
    public ApiResponse<Boolean> checkActiveItem(@Auth TokenClaim tokenClaim) {
        return ApiResponse.success(itemService.hasActiveItem(tokenClaim));
    }
}
