package com.sparta.spartatigers.global.notification.dto;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MessagePayload {

    private final AlertLevel level;                 // 심각도
    private final String subject;                   // 알림 제목
    private final String message;                   // 상세 메세지
    private final Map<String, String> metadata;     // 추가 정보
    private final String backlink;                  // 관련 링크 (그라파나 대시보드 등)
}
