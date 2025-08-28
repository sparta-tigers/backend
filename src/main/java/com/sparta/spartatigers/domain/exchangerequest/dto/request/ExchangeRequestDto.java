package com.sparta.spartatigers.domain.exchangerequest.dto.request;

import jakarta.validation.constraints.NotNull;

public record ExchangeRequestDto(
    @NotNull(message = "요청 받을 사람의 아이디는 필수입니다.") Long receiverId,
    @NotNull(message = "교환하고 싶은 아이템 아이디 입력은 필수입니다.") Long itemId
) {

}
