package com.sparta.spartatigers.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    // 인증 인가
    PASSWORD_NOT_MATCH("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ALREADY_LOGGED_IN("이미 로그인된 상태입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("로그인 후 이용 가능합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_REQUEST("권한이 없는 요청입니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND_JWT("jwt를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    NOT_SUPPORTED_SOCIAL_LOGIN("지원하지 않는 소셜 로그인입니다.", HttpStatus.BAD_REQUEST),

    // 회원
    EMAIL_ALREADY_USED("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    EMAIL_NOT_FOUND("이메일이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ALREADY_DELETED_USER("이미 탈퇴한 회원입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_ALREADY_USED("이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    ACCESS_DENIED("해당 계정의 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAME_AS_OLD_PASSWORD("새 비밀번호는 현재 비밀번호와 달라야 합니다.", HttpStatus.BAD_REQUEST),

    // 파일
    FILE_UPLOAD_FAILED("파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_FILE_EXTENSION("허용되지 않은 파일 확장자입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("파일 크기가 허용된 최대 크기를 초과했습니다.", HttpStatus.BAD_REQUEST),
    FILE_DELETE_FAILED("파일 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_FORMAT("파일 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    FILE_ALREADY_EXISTS("이미 존재하는 파일입니다.", HttpStatus.CONFLICT),
    DEFAULT_IMAGE_CANNOT_BE_DELETED("현재 기본 이미지입니다.", HttpStatus.BAD_REQUEST),

    // 매치
    MATCH_NOT_FOUND("경기 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 알람
    ALARM_NOT_FOUND("알람을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALARM_TIME_BEFORE_NOW("알람 시간이 현재보다 이전입니다.", HttpStatus.BAD_REQUEST),
    PRE_ALARM_TIME_BEFORE_NOW("알람 시간이 현재보다 이전입니다.", HttpStatus.BAD_REQUEST),

    // 채팅방
    CHATROOM_NOT_FOUND("채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // 1대1 채팅방
    TOO_MANY_MESSAGE("채팅을 너무 빠르게 입력했습니다. 잠시 후 다시 보내주세요.", HttpStatus.TOO_MANY_REQUESTS),

    // 교환 요청
    EXCHANGE_REQUEST_NOT_FOUND("교환 요청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    EXCHANGE_REQUEST_DUPLICATED("이미 교환 요청을 보냈습니다.", HttpStatus.CONFLICT),

    // 아이템
    ITEM_NOT_FOUND("아이템을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_REQUEST_OWN_ITEM("자신의 아이템에 대해 교환 요청을 할 수 없습니다.", HttpStatus.BAD_REQUEST),
    RECEIVER_NOT_OWNER("해당 아이템의 소유자에게만 요청을 보낼 수 있습니다.", HttpStatus.FORBIDDEN),
    RECEIVER_FORBIDDEN("요청을 받은 사용자만 요청을 수정할 수 있습니다.", HttpStatus.FORBIDDEN),
    LOCATION_NOT_VALID("아이템은 야구장 근처에서만 등록할 수 있습니다.", HttpStatus.BAD_REQUEST),
    ITEM_FORBIDDEN("아이템의 소유자가 아닙니다.", HttpStatus.FORBIDDEN),

    // 직관 기록
    WATCH_LIST_NOT_FOUND("직관 기록이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // 팀
    TEAM_NOT_FOUND("팀이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // 응원 팀
    FAVORITE_TEAM_NOT_FOUND("응원하는 팀이 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS_FAVORITE_TEAM("이미 응원하는 팀이 있습니다.", HttpStatus.CONFLICT),

    // 라이브 보드
    WEBSOCKET_UNAUTHORIZED("비회원은 라이브보드 메세지를 보낼 수 없습니다", HttpStatus.UNAUTHORIZED),

    // 공통
    NOT_FOUND("not found ~~", HttpStatus.NOT_FOUND),
    NOT_VALID_EXCEPTION("validation 예외 발생", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_EXCEPTION("잘못된 데이터 타입입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("서버 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}
