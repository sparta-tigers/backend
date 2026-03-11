# 프로젝트 상황 보고서

## 📋 개요

본 문서는 CodeRabbit PR Review 결과를 기반으로 현재 백엔드 프로젝트의 기술적 이슈 및 개선 필요 사항을 설계자(Gemini 3.1 Pro)에게 명확히 전달하기 위해 작성되었습니다.

## 🚨 CodeRabbit PR Review 식별 이슈

### 이슈 1: @EnableAsync 설정 누락

**위치**: `ItemLocationEventListener.java` (20-30 라인)
**문제**:

- @Async 어노테이션이 동작하지 않음
- Spring 설정 클래스에 @EnableAsync 선언 누락
- LocationService.notifyUsersNearBy()가 내부적으로 모든 예외를 catch하여 예외 전파 불가

**영향 범위**: 비동기 이벤트 처리 기능 동작 안함

### 이슈 2: ObjectMapper Bean 주입 미사용

**위치**: `ItemService.java` (44 라인)
**문제**:

- `new ObjectMapper()` 직접 생성 사용
- RedisConfig에서 설정한 JavaTimeModule 및 날짜 직렬화 설정 적용 안됨
- ReadItemResponseDto 날짜/시간 필드 직렬화 결과 불일치 가능성

**현재 코드**:

```java
private final ObjectMapper objectMapper = new ObjectMapper();
```

**영향 범위**: 날짜/시간 데이터 직렬화 시 일관성 문제

### 이슈 3: 레거시 데이터 파싱 시 공백 처리 누락

**위치**: `ItemService.java` (187-193 라인)
**문제**:

- `content.split(",")` 결과에 앞뒤 공백 포함
- 예: "[a, b]" → ["a", " b"] 형태로 파싱
- 공백 trim 처리 부재

**현재 코드**:

```java
return List.of(content.split(","));
```

**영향 범위**: 이미지 URL 데이터 정확성 문제

### 이슈 4: HTTP 상태 코드 부적절

**위치**: `AuthArgumentResolver.java` (38-40 라인)
**문제**:

- 인증 실패 시 HTTP 403 반환 (FORBIDDEN_REQUEST)
- RFC 7235/6750 규정에 따라 인증 실패는 401 Unauthorized 반환 필요
- 현재: `toLowerCase().startsWith("bearer ")` 사용

**현재 코드**:

```java
if (bearerToken == null || bearerToken.isBlank() || !bearerToken.toLowerCase().startsWith("bearer ")) {
    throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
}
```

**영향 범위**: HTTP 표준 미준수, REST API 설계 오류

## 📊 기술적 상태 요약

| 구분        | 현재 상태              | 문제 유형     | 영향도 |
| ----------- | ---------------------- | ------------- | ------ |
| 비동기 처리 | @EnableAsync 미설정    | 설정 누락     | 높음   |
| JSON 직렬화 | ObjectMapper 직접 생성 | Bean 미사용   | 중간   |
| 데이터 파싱 | 공백 처리 미흡         | 데이터 정확성 | 중간   |
| 인증 에러   | 403 대신 401 필요      | HTTP 표준     | 높음   |

## 🎯 설계자 전달 요청사항

### 1. 기술적 의사결정 필요

- **@EnableAsync 설정 위치**: 메인 애플리케이션 클래스 vs 별도 @Configuration 클래스
- **ObjectMapper Bean 관리**: 전역 설정 vs 서비스별 설정 정책
- **예외 처리 전략**: LocationService 예외 처리 방식 재정의
- **HTTP 상태 코드 표준**: 인증/인가 실패 처리 정책 수립

### 2. 아키텍처 개선 방향

- **비동기 처리 아키텍처**: 이벤트 기반 처리 표준화
- **데이터 직렬화 정책**: 일관된 JSON 처리 가이드라인
- **예외 처리 프레임워크**: 계층별 예외 처리 표준
- **API 응답 표준**: HTTP 상태 코드 사용 가이드

### 3. 코드 품질 개선

- **레거시 데이터 호환성**: 기존 데이터 마이그레이션 전략
- **보안 강화**: 인증 헤더 검증 로직 개선
- **성능 최적화**: 문자열 처리 성능 개선 방안

## 📝 현재 프로젝트 구조

### 핵심 파일 위치

```
src/main/java/com/sparta/spartatigers/
├── domain/
│   ├── item/
│   │   ├── event/ItemLocationEventListener.java
│   │   └── service/ItemService.java
│   └── ...
├── global/
│   └── aop/AuthArgumentResolver.java
├── config/
│   └── RedisConfig.java
└── ...
```

### 의존성 상태

- Spring Boot 3.x
- Java 21
- Redis (캐시)
- MySQL (데이터베이스)
- WebSocket (STOMP + SockJS)

## 🔍 다음 단계 요청

### 1. 기술적 해결책 제시

- 각 이슈별 구체적인 해결 방안 제시
- 장기적인 아키텍처 개선 방향 제안
- 성능 및 보안 고려사항 반영

### 2. 구현 우선순위 결정

- 기능적 영향도 기반 우선순위 제시
- 리스크 평가 및 완화 전략
- 단계적 구현 계획 수립

### 3. 코드 표준화 가이드

- 전체 프로젝트에 적용할 코딩 표준
- 예외 처리 및 에러 응답 가이드라인
- 비동기 처리 표준 정립

---

_작성일: 2026-03-11_  
_작성자: Cascade AI Assistant_  
_기반: CodeRabbit PR Review 결과_  
_목적: 설계자(Gemini 3.1 Pro) 기술적 의사결정 요청_
