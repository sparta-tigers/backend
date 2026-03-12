package com.sparta.spartatigers.domain.item.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.image.service.ImageStorageService;
import com.sparta.spartatigers.domain.item.dto.request.ItemCreateRequest;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemStatusRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.service.ItemService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createItem(
            @Auth TokenClaim tokenClaim,
            @RequestPart(value = "itemRequest") String itemRequestJson, // String으로 안전하게 수신
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            // ObjectMapper를 통해 수동 파싱 및 검증
            ItemCreateRequest request = objectMapper.readValue(itemRequestJson, ItemCreateRequest.class);

            // 디버깅 로그 추가
            System.out.println("=== 이미지 업로드 디버깅 ===");
            System.out.println("images 파라미터: " + images);
            System.out.println("images size: " + (images != null ? images.size() : "null"));
            
            // 1. 추상화된 스토리지에 이미지 저장 (현재는 로컬, 나중엔 S3)
            List<String> storedImageUrls = imageStorageService.uploadImages(images);
            
            System.out.println("storedImageUrls: " + storedImageUrls);
            System.out.println("========================");

            try {
                // 2. 비즈니스 로직 실행 (DTO에 URL 리스트 추가 전달)
                itemService.createItemWithImages(request, tokenClaim, storedImageUrls);
            } catch (Exception e) {
                // 3. 실패 시 업로드된 파일 삭제 (롤백)
                log.error("아이템 생성 실패, 업로드된 파일 삭제: {}", storedImageUrls, e);
                imageStorageService.deleteImages(storedImageUrls);
                throw e;
            }

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 에러: {}", e.getMessage());
            throw new InvalidRequestException(ExceptionCode.VALIDATION_ERROR);
        } catch (Exception e) {
            log.error("아이템 생성 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ApiResponse<Page<ReadItemResponseDto>> findAllItems(@Auth TokenClaim tokenClaim,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC)
        Pageable pageable,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        @RequestParam(required = false) Double radius) {

        Page<ReadItemResponseDto> response = itemService.findAllItems(tokenClaim, pageable, latitude, longitude, radius);

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
    public ApiResponse<ReadItemDetailResponseDto> findItemById(@PathVariable Long itemId) {

        ReadItemDetailResponseDto response = itemService.findItemById(itemId);

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
}
