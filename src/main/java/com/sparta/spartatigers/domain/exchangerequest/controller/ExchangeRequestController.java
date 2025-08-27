package com.sparta.spartatigers.domain.exchangerequest.controller;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.exchangerequest.dto.request.ExchangeRequestDto;
import com.sparta.spartatigers.domain.exchangerequest.dto.request.UpdateExchangeRequestDto;
import com.sparta.spartatigers.domain.exchangerequest.dto.response.ReceiveRequestResponseDto;
import com.sparta.spartatigers.domain.exchangerequest.service.ExchangeRequestService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchanges")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping
    public ApiResponse<?> createExchangeRequest(@Valid @RequestBody ExchangeRequestDto request,
        @Auth TokenClaim tokenClaim) {

        exchangeRequestService.createExchangeRequest(request, tokenClaim);

        return ApiResponse.success(null);
    }

    @GetMapping("/receive")
    public ApiResponse<Page<ReceiveRequestResponseDto>> findAllReceiveRequest(
        @Auth TokenClaim tokenClaim,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {

        Page<ReceiveRequestResponseDto> response = exchangeRequestService.findAllReceiveRequest(
            tokenClaim, pageable);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{exchangeRequestId}")
    public ApiResponse<?> updateRequestStatus(@PathVariable Long exchangeRequestId,
        @Valid @RequestBody UpdateExchangeRequestDto request, @Auth TokenClaim tokenClaim) {

        exchangeRequestService.updateRequestStatus(exchangeRequestId, request, tokenClaim);

        return ApiResponse.success(null);
    }

    @PatchMapping("/{exchangeRequestId}/complete")
    public ApiResponse<?> completeExchange(@PathVariable Long exchangeRequestId,
        @Auth TokenClaim tokenClaim) {

        exchangeRequestService.completeExchange(exchangeRequestId, tokenClaim);

        return ApiResponse.success(null);
    }
}
