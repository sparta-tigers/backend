package com.sparta.spartatigers.domain.core.trade.controller;

import com.sparta.spartatigers.domain.core.trade.dto.request.ExchangeRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.request.UpdateExchangeRequestDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ExchangeRoomResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.ReceiveRequestResponseDto;
import com.sparta.spartatigers.domain.core.trade.dto.response.SendRequestResponseDto;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;
import com.sparta.spartatigers.domain.core.trade.service.ExchangeRequestService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.aop.TokenClaim;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchanges")
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping
    public ApiResponse<ExchangeRoomResponseDto> createExchangeRequest(
        @Valid @RequestBody ExchangeRequestDto request,
        @Auth TokenClaim tokenClaim
    ) {
        Long exchangeRequestId = exchangeRequestService.createExchangeRequest(
            request,
            tokenClaim
        );
        return ApiResponse.success(
            ExchangeRoomResponseDto.created(exchangeRequestId)
        );
    }

    @GetMapping("/receive")
    public ApiResponse<Page<ReceiveRequestResponseDto>> findAllReceiveRequest(
        @Auth TokenClaim tokenClaim,
        @PageableDefault(
            sort = "createdAt",
            direction = Direction.DESC
        ) Pageable pageable
    ) {
        Page<ReceiveRequestResponseDto> response =
            exchangeRequestService.findAllReceiveRequest(tokenClaim, pageable);

        return ApiResponse.success(response);
    }

    @GetMapping("/send")
    public ApiResponse<Page<SendRequestResponseDto>> findAllSendRequest(
        @Auth TokenClaim tokenClaim,
        @PageableDefault(
            sort = "createdAt",
            direction = Direction.DESC
        ) Pageable pageable
    ) {
        Page<SendRequestResponseDto> response =
            exchangeRequestService.findAllSendRequest(tokenClaim, pageable);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{exchangeRequestId}")
    public ApiResponse<ExchangeRoomResponseDto> updateRequestStatus(
        @PathVariable Long exchangeRequestId,
        @Valid @RequestBody UpdateExchangeRequestDto request,
        @Auth TokenClaim tokenClaim
    ) {
        ExchangeRoomResponseDto response =
            exchangeRequestService.updateRequestStatus(
                exchangeRequestId,
                request,
                tokenClaim
            );
        return ApiResponse.success(response);
    }

    @PatchMapping("/{exchangeRequestId}/complete")
    public ApiResponse<?> completeExchange(
        @PathVariable Long exchangeRequestId,
        @Auth TokenClaim tokenClaim
    ) {
        exchangeRequestService.completeExchange(exchangeRequestId, tokenClaim);

        return ApiResponse.success(null);
    }

    @GetMapping("/my")
    public ApiResponse<Page<ReceiveRequestResponseDto>> findMyExchangeRequests(
        @RequestParam String role,
        @RequestParam(required = false) ExchangeStatus status,
        @Auth TokenClaim tokenClaim,
        @PageableDefault(
            sort = "createdAt",
            direction = Direction.DESC
        ) Pageable pageable
    ) {
        Page<ReceiveRequestResponseDto> response =
            exchangeRequestService.findMyExchangeRequests(
                role,
                status,
                pageable,
                tokenClaim
            );

        return ApiResponse.success(response);
    }
}
