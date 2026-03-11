# [SWE-1.5 작업 지시서] 백엔드 결함 5건 나노 단위(Nano-level) 리팩토링

## 📌 목적 및 작업 원칙

현재 백엔드 코드베이스에 트랜잭션 안정성과 데이터 정합성을 심각하게 훼손하는 5가지 결함이 발견되었다. 이 결함들을 해결하기 위해 아래 지정된 Phase 순서대로(Phase 1 -> Phase 5) 코드를 수정하라. 임의로 작업 순서를 바꾸거나 건너뛰지 마라.

---

## 🧱 Phase 1: Entity 데이터 파괴 방어 (Issue 3)

현재 `Item` 엔티티의 상태 변경 로직이 감사(Audit) 데이터인 `createdDate`를 훼손하고 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/model/Item.java`
- **지시사항**:
  - `complete()`와 `deleteItem()` 메서드에서 `this.createdDate = null;` 할당 코드를 완벽히 삭제하라. 상태가 변경되더라도 최초 생성일(createdDate)은 절대 유실되어서는 안 된다.
  - `reopen()` 로직은 상태를 `REGISTERED`로 변경하는 선에서 유지하되, `createdDate`를 건드리지 않도록 보장하라.

---

## 📨 Phase 2: DTO 상태 전이 계약 분리 (Issue 2)

API 요청 DTO가 도메인의 상태(`ItemStatus`)를 그대로 의존하여, '상태'와 '행위'가 혼용되고 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/dto/request/UpdateItemStatusRequestDto.java`
- **지시사항**:
  - `UpdateItemStatusRequestDto` 내부의 `ItemStatus status` 필드를 제거하라.
  - 클라이언트의 행위를 나타내는 전용 Enum(예: `ItemAction { COMPLETE, CANCEL, DELETE }`)을 새로 생성하고 DTO에 적용하라.
  - `ItemService.updateItemStatus` 메서드를 열어, `ItemAction` 값에 따라 명확하게 `item.complete()`, `item.reopen()`, `item.deleteItem()`을 호출하도록 스위치(Switch) 문을 수정하라. FAILED 요청이 reopen()을 호출하는 논리적 불일치를 수정하라.

---

## 🔍 Phase 3: 조회 스코프(Query Scope) 정규화 (Issue 4)

상태 변경 비즈니스 로직에서 과거/비활성 아이템까지 무분별하게 조회되는 보안/로직 취약점이 존재한다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- **지시사항**:
  - `updateItemStatus` 메서드 내부의 `itemRepository.findById(itemId)` 호출부를 찾아라.
  - 이를 다른 메서드들과 동일하게 `itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId)`로 전면 교체하라. 활성화된 당일 아이템만 상태를 변경할 수 있도록 제약을 통일하라.

---

## 💣 Phase 4: 조용한 실패(Silent Failure) 방어 (Issue 1)

교환 완료 처리 시, 필수 엔티티인 채팅방(`DirectRoom`)이 없어도 에러 없이 조용히 넘어가 데이터 정합성이 깨지고 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java`
- **지시사항**:
  - `completeExchange` 메서드 내의 `directRoomRepository.findByExchangeRequestId(exchangeRequestId).ifPresent(DirectRoom::complete);` 코드를 삭제하라.
  - 이를 `.orElseThrow()`를 사용하는 명시적 에러 처리 로직으로 교체하라. 교환 수락 시 DirectRoom을 생성하므로, 완료 시점에 Room이 없다면 `EntityNotFoundException` 또는 도메인 커스텀 예외를 던져 트랜잭션을 롤백시켜야 한다.

  ```java
  DirectRoom room = directRoomRepository.findByExchangeRequestId(exchangeRequestId)
      .orElseThrow(() -> new CustomException(ErrorCode.DIRECT_ROOM_NOT_FOUND));
  room.complete();
  ```

## 📡 Phase 5: 트랜잭션과 알림(WebSocket) 결합 분리 (Issue 5)

트랜잭션이 커밋되기 전에 WebSocket 알림이 먼저 전송되어, DB 롤백 시 클라이언트와 서버의 상태가 불일치하는 치명적 결함이 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`, `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java`, 그리고 별도의 Event / Listener 클래스
- **지시사항**:
  - `ItemService`와 `ExchangeRequestService`의 `@Transactional` 메서드 내부에 있는 `locationService.notifyUsersNearBy(...)` 직접 호출 코드를 모두 삭제하라.
  - 대신 Spring의 A`pplicationEventPublisher`를 사용하여 이벤트(예: `ItemLocationUpdatedEvent`)를 발행(Publish)하도록 수정하라.
  - 별도의 `@Component` 클래스(예: `ItemLocationEventListener`)를 생성하고, `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`를 사용하여 트랜잭션이 성공적으로 커밋된 직후에만 `locationService.notifyUsersNearBy`가 실행되도록 보장하라.

## 🎯 검증 지시

각 Phase의 수정이 완료될 때마다 커밋을 분리하여 작성하라. 5개의 Phase가 모두 끝나면 작업 내역을 요약하여 보고하라.
