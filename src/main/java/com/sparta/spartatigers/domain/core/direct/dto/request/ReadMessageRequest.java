package com.sparta.spartatigers.domain.core.direct.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadMessageRequest {
    private Long roomId;
    private Long messageId;
}
