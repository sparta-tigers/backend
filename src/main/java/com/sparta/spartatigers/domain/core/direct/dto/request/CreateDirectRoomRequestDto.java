package com.sparta.spartatigers.domain.core.direct.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDirectRoomRequestDto {

    @NotNull(message = "교환 요청 id를 입력해 주세요.")
    private Long exchangeRequestId;
}
