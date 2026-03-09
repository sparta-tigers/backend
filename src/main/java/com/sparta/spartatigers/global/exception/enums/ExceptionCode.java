package com.sparta.spartatigers.global.exception.enums;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    // 인증/인가
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "비밀번호가 일치하지 않습니다.", LogLevel.DEBUG),
    ALREADY_LOGGED_IN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 로그인된 상태입니다.", LogLevel.DEBUG),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "로그인 후 이용 가능합니다.", LogLevel.DEBUG),
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, ErrorCode.E403, "권한이 없는 요청입니다.", LogLevel.DEBUG),
    NOT_FOUND_JWT(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "jwt를 찾을 수 없습니다.", LogLevel.DEBUG),
    NOT_SUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "지원하지 않는 소셜 로그인입니다.", LogLevel.DEBUG),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "인증된 사용자만 수행할 수 있는 요청입니다.", LogLevel.DEBUG),
    AUTHORIZATION_ERROR(HttpStatus.FORBIDDEN, ErrorCode.E403, "권한이 부족한 유저입니다.", LogLevel.DEBUG),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "유효하지 않은 리프레시 토큰입니다.", LogLevel.DEBUG),

    // 회원
    EMAIL_ALREADY_USED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 사용 중인 이메일입니다.", LogLevel.DEBUG),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "이메일이 존재하지 않습니다.", LogLevel.DEBUG),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "유저가 존재하지 않습니다.", LogLevel.DEBUG),
    ALREADY_DELETED_USER(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 탈퇴한 회원입니다.", LogLevel.DEBUG),
    NICKNAME_ALREADY_USED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 사용 중인 닉네임입니다.", LogLevel.DEBUG),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, ErrorCode.E403, "해당 계정의 접근 권한이 없습니다.", LogLevel.DEBUG),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "현재 비밀번호가 일치하지 않습니다.", LogLevel.DEBUG),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "새 비밀번호는 현재 비밀번호와 달라야 합니다.", LogLevel.DEBUG),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "카카오 로그인에 실패했습니다.", LogLevel.DEBUG),
    OAUTH_USERINFO_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "카카오 사용자 정보를 가져오지 못했습니다.", LogLevel.DEBUG),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "카카오 로그인 시 이메일 제공 동의가 필요합니다.", LogLevel.DEBUG),

    // 파일
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "파일 업로드에 실패했습니다.", LogLevel.ERROR),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "파일을 찾을 수 없습니다.", LogLevel.DEBUG),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, ErrorCode.E400, "허용되지 않은 파일 확장자입니다.", LogLevel.DEBUG),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "파일 크기가 허용된 최대 크기를 초과했습니다.", LogLevel.DEBUG),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "파일 삭제에 실패했습니다.", LogLevel.ERROR),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, ErrorCode.E400, "파일 형식이 올바르지 않습니다.", LogLevel.DEBUG),
    FILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 존재하는 파일입니다.", LogLevel.DEBUG),
    DEFAULT_IMAGE_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "현재 기본 이미지입니다.", LogLevel.DEBUG),

    // 매치
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "경기 일정을 찾을 수 없습니다.", LogLevel.DEBUG),

    // 경기장
    STADIUM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "구장을 찾을 수 없습니다.", LogLevel.DEBUG),

    // 알람
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "알람을 찾을 수 없습니다.", LogLevel.DEBUG),
    ALARM_TIME_ALREADY_PASSED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "현재보다 과거 시점에 알람을 맞출 수 없습니다", LogLevel.DEBUG),
    BOOKING_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "예매 오픈 일정을 계산할 수 없습니다.", LogLevel.DEBUG),
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "멤버쉽을 찾을 수 없습니다.", LogLevel.DEBUG),
    INVALID_PRE_ALARM_TIME(HttpStatus.BAD_REQUEST, ErrorCode.E400, "알람시간은 세시간을 넘길 수 없습니다.", LogLevel.DEBUG),

    // 채팅방
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "채팅방이 존재하지 않습니다.", LogLevel.DEBUG),

    // 1대1 채팅방
    TOO_MANY_MESSAGE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "채팅을 너무 빠르게 입력했습니다. 잠시 후 다시 보내주세요.", LogLevel.DEBUG),

    // 교환 요청
    EXCHANGE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "교환 요청을 찾을 수 없습니다.", LogLevel.DEBUG),
    EXCHANGE_REQUEST_DUPLICATED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 교환 요청을 보냈습니다.", LogLevel.DEBUG),

    // 아이템
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "아이템을 찾을 수 없습니다.", LogLevel.DEBUG),
    CANNOT_REQUEST_OWN_ITEM(HttpStatus.BAD_REQUEST, ErrorCode.E400, "자신의 아이템에 대해 교환 요청을 할 수 없습니다.", LogLevel.DEBUG),
    RECEIVER_NOT_OWNER(HttpStatus.FORBIDDEN, ErrorCode.E403, "해당 아이템의 소유자에게만 요청을 보낼 수 있습니다.", LogLevel.DEBUG),
    RECEIVER_FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "요청을 받은 사용자만 요청을 수정할 수 있습니다.", LogLevel.DEBUG),
    LOCATION_NOT_VALID(HttpStatus.BAD_REQUEST, ErrorCode.E400, "아이템은 야구장 근처에서만 등록할 수 있습니다.", LogLevel.DEBUG),
    ITEM_FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "아이템의 소유자가 아닙니다.", LogLevel.DEBUG),

    // 직관 기록
    WATCH_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "직관 기록이 존재하지 않습니다.", LogLevel.DEBUG),

    // 팀
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "팀이 존재하지 않습니다.", LogLevel.DEBUG),

    // 응원 팀
    FAVORITE_TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "응원하는 팀이 없습니다.", LogLevel.DEBUG),
    ALREADY_EXISTS_FAVORITE_TEAM(HttpStatus.BAD_REQUEST, ErrorCode.E400, "이미 응원하는 팀이 있습니다.", LogLevel.DEBUG),

    // 라이브 보드
    WEBSOCKET_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "비회원은 라이브보드 메세지를 보낼 수 없습니다", LogLevel.DEBUG),

    // FCM
    FCM_MESSAGE_NOT_SENT(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.E503, "FCM 메세지 송신에 실패했습니다.", LogLevel.ERROR),

    // 공통
    INVALID_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, ErrorCode.E400, "잘못된 데이터 타입입니다.", LogLevel.DEBUG),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "요청값 검증에 실패 했습니다", LogLevel.DEBUG),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "예상하지 못한 예외가 발생 했습니다.", LogLevel.ERROR),

    // 순위
    POSTSEASON_RANKING_UNAVAILABLE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "포스트시즌은 순위 조회가 불가능합니다.", LogLevel.DEBUG);




    private final HttpStatus httpStatus;
    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;
}
