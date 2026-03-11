# [SWE-1.5 작업 지시서] 백엔드 결함 5건 나노 단위(Nano-level) 리팩토링 (Part 2)

## 📌 목적 및 작업 원칙

현재 백엔드 코드베이스에 실시간 데이터 동기화 누락, 보안 취약점, 동시성(Concurrency) 문제, 그리고 API 하위 호환성 붕괴 등 6가지 추가 결함이 발견되었다. 아래 지정된 Phase 순서대로(Phase 6 -> Phase 10) 코드를 수정하라.

---

## 📡 Phase 6: 실시간 데이터 동기화(ADD_ITEM) 누락 복구 (Issue 1)

아이템이 재오픈(reopen)되었을 때, 주변 사용자들의 지도와 리스트에 해당 아이템이 다시 나타나지 않는 치명적 결함이 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- **지시사항**:
  - `updateItemStatus` 메서드 내부의 `FAILED` 상태 처리 블록(`item.reopen()`)을 찾아라.
  - `item.reopen()` 호출 직후, 신규 아이템 등록 시와 동일하게 `ADD_ITEM` 이벤트를 브로드캐스트하는 로직을 추가하라. (이전 Phase 5에서 이벤트 발행 방식으로 변경했다면 `ItemLocationUpdatedEvent`를 발행할 것).

  ```java
  // 예시
  item.reopen();
  ReadItemResponseDto reopenedItemDto = ReadItemResponseDto.from(item, this);
  locationService.notifyUsersNearBy(item.getUser().getId(), "ADD_ITEM", reopenedItemDto);
  ```

## 🔒 Phase 7: 디바이스 토큰 민감 정보 로깅 차단 (Issue 2)

푸시 토큰이 로그 파일에 원문 그대로 노출되는 심각한 보안 취약점(PII 노출)이 존재한다.

- 대상 파일: `src/main/java/com/sparta/spartatigers/domain/notification/controller/DeviceTokenController.java`
- 지시사항:
  - log.info 내부의 `request.token().substring(0, Math.min(request.token().length(), 20))` 코드를 즉시 삭제하라.
  - 로그에는 민감한 원문 대신 토큰의 길이(`request.token().length()`)나 마스킹된 정보(예: \*\*\*)만 남기도록 수정하라.

## 💾 Phase 8: 디바이스 토큰 영구 저장 API 구현 (Issue 3)

클라이언트가 푸시 토큰을 전송해도 백엔드가 이를 저장하지 않아(TODO 방치) 푸시 알림이 원천적으로 불가능한 상태다.

- 대상 파일: `DeviceTokenController.java`, `DeviceTokenService.java` (생성 필요), `User` 엔티티 또는 `DeviceToken` 엔티티

- 지시사항:
  - `DeviceTokenController`의 `register` 메서드에 있는 `// TODO: 필요 시 DB에 디바이스 토큰 영구 저장...` 주석을 지워라.
  - 실제 DB에 토큰을 저장(`Upsert`)하는 서비스 로직을 구현하여 연결하라.
  - (이미 User 엔티티에 `deviceToken` 컬럼이 있다면 해당 값을 업데이트하고, 별도 테이블이 있다면 해당 `Repository`를 통해 저장하라).

## 🏎️ Phase 9: 완료된 채팅방 메시지 전송 경쟁 상태(Race Condition) 차단 (Issue 4)

채팅방 상태 확인 후 메시지를 저장하는 찰나의 순간에 방이 완료 처리되면 메시지가 비정상적으로 저장되는 동시성 문제가 있다.

- 대상 파일: `src/main/java/com/sparta/spartatigers/domain/stompchat/service/ExchangeChatService.java`
- 지시사항:
  - 낙관적 락(Optimistic Lock)을 적용하거나, 조회 쿼리에 비관적 락(Pessimistic Lock - FOR UPDATE)을 걸어 동시성을 제어하라.
  - 또는 가장 단순하고 확실한 방법으로, 메시지 저장 트랜잭션의 격리 수준(Isolation Level)을 높이거나 DB 레벨의 제약 조건(채팅방 완료 상태 시 Insert Trigger 차단 등)을 활용해 경쟁 상태를 원천 차단하라. JPA를 사용 중이라면 @Lock(LockModeType.PESSIMISTIC_WRITE)를 권장한다.

## 🛡️ Phase 10: HTTP 표준(RFC 6750) 인증 스키마 대소문자 무시 (Issue 6)

Bearer 토큰 파싱 시 대소문자를 엄격하게 구분하여 표준 라이브러리들의 요청을 거부하는 문제가 있다.

- 대상 파일: `src/main/java/com/sparta/spartatigers/global/aop/AuthArgumentResolver.java`
- 지시사항:
  - `!bearerToken.startsWith("Bearer ")` 코드를 `!bearerToken.toLowerCase().startsWith("bearer ")` 또는 `!StringUtils.startsWithIgnoreCase(bearerToken, "Bearer ")`로 수정하여 대소문자를 무시하도록 변경하라.
  - 토큰을 파싱(substring)할 때도 대소문자 무관하게 7번째 인덱스부터 자를 수 있도록 안전하게 처리하라.

## 🎯 검증 지시

위 Phase 6 ~ 10의 작업이 완료될 때마다 커밋을 분리하라. 모든 작업이 끝나면 [백엔드 트러블슈팅 2차 완료] 메시지를 출력하라.
