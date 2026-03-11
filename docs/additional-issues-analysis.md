# 추가 발견된 백엔드 문제 상황 분석 보고서

## 개요

CodeRabbit PR 리뷰에서 추가로 발견된 6가지 항목에 대한 현재 코드 상황 분석. Gemini 3.1 Pro 모델이 설계 결정을 내릴 수 있도록 실제 코드 상태를 객관적으로 기록한다.

## 1. 재오픈 경로에서 `ADD_ITEM` 브로드캐스트 누락

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
**위치**: Lines 103-105

```java
if (request.status() == ItemStatus.FAILED) {
    item.reopen();
    return;  // ADD_ITEM 브로드캐스트 없음
}
```

**비교**: 신규 등록 시 (Lines 81-83)

```java
ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item, this);
locationService.notifyUsersNearBy(user.getId(), "ADD_ITEM", newItemDto);
```

**비교**: 완료/삭제 시 (Lines 96-99, 108-111)

```java
Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);
```

### 분석

- `FAILED → reopen()` 경로에서는 `ADD_ITEM` 브로드캐스트가 없음
- 근처 목록을 실시간으로 유지하는 클라이언트에 아이템이 다시 나타나지 않음
- 아이템이 다시 활성화되었지만 주변 사용자들은 알림을 받지 못함

## 2. 디바이스 토큰 민감 정보 로깅 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/notification/controller/DeviceTokenController.java`
**위치**: Lines 33-38

```java
log.info(
    "[DeviceToken] userId={}, deviceType={}, tokenPrefix={}",
    userId,
    request.deviceType(),
    request.token().substring(0, Math.min(request.token().length(), 20))  // 토큰 원문 로깅
);
```

### 분석

- 토큰 prefix와 사용자 계정(`userId`)이 함께 로깅되어 재식별 가능
- `request.token().substring(0, 20)`으로 토큰 원문 일부가 로그에 기록됨
- 운영 로그에는 토큰 원문/일부 대신 길이, 해시, 등록 결과 같은 비민감 메타데이터만 안전
- 현재는 TODO 주석만 있고 실제 저장 로직 없음

### 보안 위험

- 민감한 토큰 정보가 로그 파일에 영구적으로 기록됨
- 로그 접근 권한이 있는 모든 사람이 토큰 정보 획득 가능
- 재식별 가능한 정보로 인한 보안 취약점

## 3. 디바이스 토큰 미저장 API 계약 위반

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/notification/controller/DeviceTokenController.java`
**위치**: Lines 40-42

```java
// TODO: 필요 시 DB에 디바이스 토큰 영구 저장 및 중복/만료 관리 추가

return ApiResponse.success("디바이스 토큰이 정상적으로 등록되었습니다.");
```

### 분석

- 토큰을 로그에만 남기고 실제로는 저장하지 않음
- 이 엔드포인트를 호출해도 이후 푸시 발송에 사용할 등록 정보가 남지 않음
- `register`라는 메서드명과 성공 메시지 때문에 클라이언트는 등록이 완료됐다고 오해
- API 계약 위반: 성공 응답하지만 실제로는 데이터가 저장되지 않음

### 영향

- 푸시 알림 기능이 정상적으로 동작하지 않음
- 클라이언트는 등록 성공으로 착각하지만 실제로는 토큰이 없음
- 디바이스 토큰 관리의 신뢰성 저하

## 4. 완료된 채팅방 메시지 전송 경쟁 상태 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/stompchat/service/ExchangeChatService.java`
**위치**: Lines 65-68

```java
if (room.isCompleted()) {
    log.warn("[sendMessage] 완료된 채팅방 전송 차단 - roomId: {}, senderId: {}", roomId, senderId);
    throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
}
```

### 분석

- 조회된 엔티티 상태를 한 번만 확인하므로 경쟁 상태 발생 가능성
- `room.isCompleted()` 체크 직후 다른 트랜잭션이 `room.complete()`를 커밋하면 현재 트랜잭션은 그대로 메시지 저장 가능
- "완료된 방은 쓰기 금지" 규칙이 우회될 수 있음

### 경쟁 조건

1. 트랜잭션 A: `room.isCompleted()` = false 확인
2. 트랜잭션 B: `room.complete()` 커밋 (상태를 true로 변경)
3. 트랜잭션 A: 메시지 저장 및 커밋 (완료된 방에 메시지 저장)

### 위험

- 완료된 채팅방에 메시지가 저장될 수 있음
- 비즈니스 규칙 위반
- 데이터 정합성 문제

## 5. 기존 API 경로 변경으로 클라이언트 호환성 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/user/controller/UserRestController.java`
**위치**: Lines 26, 34

```java
@PostMapping("/api/users")  // 기존: /api/v1/users
public ApiResponse<?> register(@RequestBody @Valid UserRegisterRequest request) {

@GetMapping("/api/users/me")  // 기존: /api/v1/users/me
public ApiResponse<TokenClaim> me(@Auth TokenClaim tokenClaim) {
```

### 개발자 코멘트

"api 엔드포인트에서 v1 태그는 제거했습니다. 따라서 `/api/users/` , `/api/users/me` 가 올바른 형식입니다."

### 분석

- 기존 클라이언트/문서/HTTP 예제는 `/api/v1/users`와 `/api/v1/users/me`를 호출
- 새 경로로만 변경하면 기존 모든 클라이언트가 404 오류 발생
- 버전 정책을 명확히 정리하지 않은 상태에서의 변경
- 하위 호환성 고려 없는 변경

### 영향

- 기존 클라이언트 전체 동작 불가
- API 문서와 실제 불일치
- 배포 시 서비스 장애 가능성

## 6. Bearer 접두사 대소문자 구분 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/global/aop/AuthArgumentResolver.java`
**위치**: Line 38

```java
if (bearerToken == null || bearerToken.isBlank() || !bearerToken.startsWith("Bearer ")) {
    throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
}
```

### 분석

- `startsWith("Bearer ")`는 대소문자를 구분함
- RFC 7235와 RFC 6750에서 정의한 유효한 Authorization 헤더인 `authorization: bearer <token>` 요청을 거부
- HTTP 표준에 따라 `auth-scheme`은 대소문자를 무시하여 매칭해야 함
- 현재는 "Bearer"만 허용하고 "bearer"는 거부

### 표준 위반

- HTTP 표준 위반 (RFC 7235, RFC 6750)
- 일부 클라이언트 라이브러리에서 소문자로 보낼 경우 인증 실패
- 엄격한 표준 준수 부족

## 요약

6가지 항목 모두 CodeRabbit이 지적한 대로 현재 코드에 실제로 존재하는 문제들임. 각 항목은 실시간 통신, 보안, 데이터 정합성, API 호환성, 표준 준수 측면에서 개선이 필요한 상황임.

---

_작성일: 2026-03-11_  
_분석 대상: 백엔드 Spring Boot 코드_  
_목적: Gemini 3.1 Pro 설계 결정 지원_
