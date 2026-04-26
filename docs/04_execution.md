# Task Prompt: 04. 비즈니스 로직 및 상태 관리 (Business Logic & State)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **상태 돌연변이 후 참조 (Mutated State Reference):** `ItemService`의 CANCEL 분기에서 `req.updateStatus(REJECTED)`로 상태를 덮어쓴 직후, 조건문에서 `req.getStatus() == ACCEPTED`인지 검사하는 논리적 모순이 발생함. 이로 인해 교환 취소 시 채팅방에 시스템 메시지가 절대 발행되지 않는 치명적인 상태 불일치가 발생.
- **불완전한 상태 게이트 (Incomplete State Gate):** `ExchangeChatService`에서 상태를 분기할 때 명시적인 `switch-case`를 사용하지 않아, `COMPLETED` 상태가 `room.isCompleted()` 방어선을 뚫고 들어왔을 때 모호한 예외(`EXCHANGE_NOT_ACCEPTED_REJECTED`)로 퉁쳐지는 문제.
- **권한 검증의 파편화 (Fragmented Single Source of Truth):** `DirectRoomService` 내에서 `getRoomItem`은 `ExchangeRequest` 기준으로 권한을 검증하는 반면, `getMessagesAfter`는 `DirectRoom` 기준으로 권한을 검증하고 있음. 동일한 도메인 액션에 두 개의 진실의 원천(Source of Truth)이 존재하여 정합성이 깨질 위험이 있음.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **결정론적 상태 전이 (Deterministic State Transition):** 객체의 상태를 변경(Mutation)할 때는 반드시 변경 이전의 상태(Previous State)를 불변 변수로 캡처해두고 로직을 전개한다. "마법처럼 현재 상태가 내가 원하는 과거를 기억해줄 것"이라고 기대하지 마라.
- **모든 도메인 상태의 명시적 매핑 (Exhaustive State Mapping):** 비즈니스 핵심 상태(PENDING, ACCEPTED, REJECTED, COMPLETED)를 다룰 때는 `if-else`로 대충 묶거나 `default`로 예외를 던지는 것을 금지한다. `switch` 문을 사용해 모든 상태가 1:1로 대응되는 명확한 ExceptionCode(Task 01에서 분리한 코드)를 던지게 하여, 새로운 상태 추가 시 컴파일 타임/로직 뎁스에서 에러를 뿜도록 강제한다.
- **단일 진실의 원천 (Single Source of Truth):** 채팅방 내부의 권한 및 상태 검증은 무조건 `ExchangeRequest` 엔티티 하나만을 기준으로 통일하여 결합도와 예외 발생 확률을 낮춘다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- `src/main/java/com/sparta/spartatigers/domain/stompchat/service/ExchangeChatService.java`
- `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java`

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
  - **기존 함수/클래스** : `CANCEL` 분기 내 `activeRequests` 순회 로직
  - **변경 사항** :
    - [ ] 루프 진입 즉시 `ExchangeStatus previousStatus = req.getStatus();` 로 이전 상태를 명시적으로 저장할 것.
    - [ ] `req.updateStatus(...)` 호출 이후 시스템 메시지 발행 조건문을 `if (previousStatus == ExchangeStatus.ACCEPTED)` 로 수정할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/stompchat/service/ExchangeChatService.java`
  - **기존 함수/클래스** : `ACCEPTED` 게이트 (상태 체크 로직)
  - **변경 사항** :
    - [ ] 기존의 단순 분기문을 삭제하고, `switch (exchangeStatus)`를 도입할 것.
    - [ ] `PENDING`, `REJECTED`, `COMPLETED` 상태에 대해 (Task 01에서 정의한) 명확하고 개별적인 ExceptionCode를 각각 명시적으로 throw 할 것. `EXCHANGE_NOT_ACCEPTED_REJECTED` 같은 모호한 통합 코드는 사용하지 말 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java`
  - **기존 함수/클래스** : `getMessagesAfter(...)`
  - **변경 사항** :
    - [ ] 권한 체크 시 `room.getSender().getId()` 및 `room.getReceiver().getId()`를 사용하는 부분을 `room.getExchangeRequest().getSender().getId()` 및 `getReceiver().getId()` 로 변경할 것.
    - [ ] `getRoomItem` 메서드의 권한 검증 방식과 완전히 동일한 로직(단일 진실의 원천)으로 맞출 것.

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **상태 덮어쓰기 후 과거 추론 금지:** 값(State)을 변경한 뒤에, 그 변경된 객체에게 "너 아까 무슨 상태였지?"라고 묻는 멍청한 코드를 작성하지 마라.
- **Catch-all 상태 처리 금지:** 도메인 Enum(`ExchangeStatus`)을 다루면서 `else`나 `default` 블록에 주요 비즈니스 로직(혹은 중요한 예외 처리)을 숨기지 마라.
- **다중 진실의 원천(Multiple Sources of Truth) 유지 금지:** 한 서비스 클래스 안에서 같은 권한을 검증하는데 A 메서드는 Room을 보고, B 메서드는 Request를 보는 식의 파편화를 절대 허용하지 않는다.

## 6. 최종 검증 (Verification)

- 교환 취소(CANCEL) 시 ACCEPTED 상태였던 채팅방에 정상적으로 `STATUS_UPDATED` 시스템 메시지가 발행되는가?
- `ExchangeChatService`에서 각 상태(PENDING, REJECTED, COMPLETED) 접근 시, 클라이언트 프론트엔드가 정확히 구분할 수 있는 각각 다른 ExceptionCode가 내려가는가?
- `getMessagesAfter` 와 `getRoomItem` 의 권한 검증 기준이 `ExchangeRequest` 로 완벽하게 통일되었는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
