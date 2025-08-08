package com.sparta.spartatigers.domain.item.controller;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.item.dto.request.CreateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.service.ItemService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ApiResponse<ItemResponseDto> createItem(
        @Valid @RequestBody CreateItemRequestDto request,
        @Auth TokenClaim tokenClaim) {

        ItemResponseDto response = itemService.createItem(request, tokenClaim);

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<Page<ReadItemResponseDto>> findAllItems(@Auth TokenClaim tokenClaim,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC)
        Pageable pageable) {

        Page<ReadItemResponseDto> response = itemService.findAllItems(tokenClaim, pageable);

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

        return ApiResponse.success();
    }

    @PatchMapping("/{itemId}")
    public ApiResponse<ItemResponseDto> updateItem(@Auth TokenClaim tokenClaim,
        @PathVariable Long itemId, @Valid @RequestBody UpdateItemRequestDto request) {

        ItemResponseDto response = itemService.updateItem(tokenClaim, itemId, request);

        return ApiResponse.success(response);
    }
}
