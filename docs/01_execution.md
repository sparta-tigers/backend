# Task Prompt: 01. 예외 핸들링 및 NPE 방어 (Exception & NPE Handling)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **상태 코드의 비결정성 (ExceptionCode):** 클라이언트가 상태를 명확히 구분해야 하는 `REJECTED`와 `COMPLETED`를 하나의 ExceptionCode로 묶어 응답함으로써, 프론트엔드가 UI 분기를 위해 에러 메시지 문자열을 파싱해야 하는 안티패턴(Magic String Parsing)을 유발함.
- **과도한 예외 흡수 (ItemService):** `sendNotificationSafely`에서 포괄적인 `catch (Exception e)`를 사용하여 로직 결함(예: NPE, 쿼리 에러 등)까지 조용히 삼키고 있음. 이는 시스템 모니터링 사각지대를 만듦. 또한, JSON 역직렬화 실패 시 Stack Trace를 남기지 않아 데이터 손상 추적을 불가능하게 함.
- **불안전한 객체 참조 (UserConnectService):** `targetUserIds`가 null일 가능성이 있음에도 방어 로직 없이 `.size()`를 호출하여 런타임에 즉각적인 `NullPointerException`을 발생시킴. 하위 레이어(RedisRegistry)의 null-safe 처리에 암묵적으로 의존하는 것은 취약한 설계임.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **Zero Magic Error Handling:** 모든 에러 코드는 1:1로 명확한 비즈니스 상태를 대변해야 한다. 포괄적인 Exception Catch는 당장 눈앞의 에러를 숨길 뿐 기술 부채를 키우므로, 정확히 예상되는 타겟 예외(`FirebaseException`)만 잡고 나머지는 전파시켜야 한다.
- **명시적 가드 (Explicit Guard Clauses):** 파라미터 방어는 하위 레이어에 책임을 떠넘기지 않고, 메서드 진입점에서 `Early Return` 패턴을 통해 명시적으로 처리하여 NPE를 원천 차단한다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/global/exception/enums/ExceptionCode.java`
- `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- `src/main/java/com/sparta/spartatigers/domain/directRoom/service/UserConnectService.java`

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/global/exception/enums/ExceptionCode.java`
  - **기존 함수/클래스** : `EXCHANGE_NOT_ACCEPTED_REJECTED` Enum 필드
  - **변경 사항** :
    - [ ] 기존 `EXCHANGE_NOT_ACCEPTED_REJECTED` 삭제.
    - [ ] `EXCHANGE_ALREADY_COMPLETED("이미 교환 완료된 아이템입니다.")` 추가.
    - [ ] `EXCHANGE_ALREADY_REJECTED("이미 거절된 교환 요청입니다.")` 추가.
    - [ ] (필요시 이 코드를 사용하는 서비스 레이어의 throw 부분도 함께 분기 처리할 것).
  - **주의할 의존성** : 프론트엔드와 맞물린 에러 코드이므로 상태 이름이 명시적이어야 함.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
  - **기존 함수/클래스** : `sendNotificationSafely(...)` 및 역직렬화 관련 블록
  - **변경 사항** :
    - [ ] `sendNotificationSafely`의 `catch (Exception e)`를 `catch (FirebaseException e)`로 축소. 예상치 못한 런타임 예외는 잡지 말고 그대로 던져서 시스템 로그에 찍히게 할 것.
    - [ ] JSON 역직렬화 예외 처리 블록에서 `log.error("...", e.getMessage())`를 `log.error("...", e)`로 변경하여 짤린 메시지 대신 완전한 Stack Trace를 로깅하도록 강제.
  - **주의할 의존성** : `com.sparta.spartatigers.global.exception.external.FirebaseException` 임포트 확인.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/service/UserConnectService.java`
  - **기존 함수/클래스** : `getOnlineStatuses(List<Long> targetUserIds)` (또는 유사한 타겟 유저 체크 메서드)
  - **변경 사항** :
    - [ ] 메서드 진입점 최상단에 `if (targetUserIds == null || targetUserIds.isEmpty()) { return Collections.emptyMap(); }` (또는 해당 반환 타입에 맞는 빈 객체) 가드 클로즈를 명시적으로 작성.
    - [ ] 절대 하위 레이어가 null을 알아서 처리해줄 것이라고 가정하고 `.size()` 등을 먼저 호출하지 말 것.
  - **주의할 의존성** : `java.util.Collections`

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **포괄적 예외 처리 절대 금지:** `catch (Exception e)`나 `catch (Throwable t)`를 사용하여 알 수 없는 시스템 버그까지 조용히 덮는 행위를 엄격히 금지한다.
- **에러 코드 혼용 금지:** 프론트엔드가 UI 렌더링 조건을 에러 "메시지 문자열"에 의존하게 만들지 마라. 상태는 DTO와 Enum 코드로만 명확히 결정되어야 한다.
- **하위 레이어 의존형 방어 금지:** 파라미터가 null일 가능성이 있다면 진입점에서 즉시 검증하고 반환하라. "Redis가 알아서 null 무시하겠지" 같은 암묵적 의존은 허용하지 않는다.

## 6. 최종 검증 (Verification)

- `UserConnectService`에 `null`을 주입했을 때 NPE 대신 안전하게 빈 컬렉션이 반환되는가?
- 알림 발송 중 `NullPointerException`이나 `IllegalArgumentException`이 발생했을 때, 이를 삼키지 않고 상위로 제대로 전파(Throw)하는가?
- `REJECTED`와 `COMPLETED` 상태 요청 시 클라이언트가 각각 다른 Error Code를 수신할 수 있는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
