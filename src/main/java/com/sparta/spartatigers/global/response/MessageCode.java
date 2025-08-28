package com.sparta.spartatigers.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageCode {

    // 인증/인가

    // 회원
    USER_REQUEST_SUCCESS("회원 요청을 성공적으로 보냈습니다."),
    PROFILE_DELETE_SUCCESS("프로필이 성공적으로 삭제되었습니다."),
    USER_NICKNAME_UPDATE_SUCCESS("닉네임이 성공적으로 변경되었습니다."),
    USER_PASSWORD_UPDATE_SUCCESS("비밀번호가 성공적으로 변경되었습니다."),
    USER_DELETED("회원 탈퇴가 성공적으로 되었습니다."),

    // 라이브 보드

    // 알람
    ALARM_REQUEST_SUCCESS("알람 요청을 성공적으로 보냈습니다."),
    // 채팅방

    // 교환 요청
    EXCHANGE_REQUEST_SUCCESS("교환 요청을 성공적으로 보냈습니다."),
    UPDATE_EXCHANGE_REQUEST_SUCCESS("교환 요청 상태 변경을 성공했습니다."),
    COMPLETE_EXCHANGE_SUCCESS("교환을 성공적으로 완료했습니다."),

    // 아이템
    ITEM_DELETE_SUCCESS("아이템이 성공적으로 삭제되었습니다."),
    // 직관 기록
    WATCH_LIST_DELETED("직관 기록이 삭제되었습니다.")

// 공통
;
    private final String message;
}
