# PR Comments

## 다중 인스턴스 배포 시 스케줄러 중복 실행 위험

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java (1)`
- 라인: `58-65`:

### 문제 1

- 이 스케줄러는 모든 인스턴스에서 동시에 실행될 수 있으며, 같은 파일을 두 인스턴스가 동시에 처리하려고 시도하면 NoSuchFileException 로그 폭주 또는 의도치 않은 경합이 발생합니다.
- 다중 노드 환경이라면 ShedLock 등 분산 락을 도입하거나, 단일 인스턴스에서만 동작하도록 프로파일/플래그로 제한하는 것이 안전합니다.
- 또한 cron `0 0 3 * * *`은 JVM 기본 타임존에 의존합니다.
- 컨테이너 환경에서는 UTC가 기본일 수 있어 의도한 KST 03:00이 아닐 수 있습니다.
- `@Scheduled(cron = "...", zone = "Asia/Seoul")`로 명시하는 것을 권장합니다.

## `afterCommit` 람다가 캡처하는 `sender`/`exchangeRequest` 의 detached 상태 주의

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java (4)`
- 라인: `286-301`

### 문제 2

- `sender.getDeviceToken()` 및 `sender.getId()` 가 afterCommit 시점(트랜잭션 커밋 직후, 영속성 컨텍스트 종료 후)에 호출됩니다.
- `User` 엔티티의 lazy 필드를 추가로 접근하는 경우 `LazyInitializationException` 이 날 수 있습니다.
- 현재는 `getDeviceToken()` / `getId()` 만 접근해 EAGER 로딩 필드일 가능성이 높지만, 향후 메시지 본문에 lazy 필드를 끼워 넣을 때 함정이 됩니다.
- afterCommit 등록 전에 필요한 값들을 `final` 로컬 변수로 추출해 캡처하는 패턴을 권장드립니다.

## `findAllReceiveRequest` / `findAllSendRequest` 메서드에 `@Transactional(readOnly = true)` 누락 — Lazy 로딩 시 예외 위험

### 문제 파일 및 라인 3

- 파일: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java`
- 라인: `80-94`

### 문제 3

- 두 메서드는 트랜잭션 어노테이션이 없으면서 `mapToReceiveResponse` / `mapToSendResponse` 호출 내부의 `getRoomIdMap` 에서 `directRoomRepository.findByExchangeRequestIdIn` 을 실행한 뒤 `room.getExchangeRequest()` 에 접근합니다.
- DirectRoom 의 exchangeRequest 필드는 LAZY 로딩으로 설정되어 있으므로, 트랜잭션 없이 세션이 종료되면 이 시점에서 `LazyInitializationException` 이 발생할 수 있습니다.
- `@Transactional(readOnly = true)` 를 두 메서드에 추가하여 `findByExchangeRequestIdIn` 호출과 lazy 엔티티 접근이 동일한 트랜잭션 컨텍스트 내에서 실행되도록 보장하는 것이 필요합니다.

## `getRoomsForUser` 에 `@Transactional(readOnly = true)` 권장 + opponentIds 중복 제거

### 문제 파일 및 라인 4

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java (3)`
- 라인: `71-106`

### 문제 4

- 두 가지 의견입니다.
  1. 이 메서드는 트랜잭션 어노테이션이 없는데, fetch join 으로 대부분의 연관 엔티티를 로드하더라도 이후 `DirectRoomResponseDto.from(room, ...)` 내부에서 추가적인 lazy access 가 발생하면 `LazyInitializationException` 위험이 있습니다. 또한 `directMessageRepository.countUnreadMsgInBatch(...)` 와 `userConnectService.getOnlineStatuses(...)` 호출까지 단일 트랜잭션 컨텍스트로 묶는 것이 정합성/일관성 측면에서 안전합니다. `@Transactional(readOnly = true)` 를 부착해 주세요.
  2. `opponentIds` 는 같은 유저가 여러 방의 상대인 경우 중복이 들어갑니다. Redis 호출이 약간 더 발생할 뿐이지만 `.distinct().toList()` 로 정리하는 편이 깔끔합니다.

### 제안 패치 4

```diff
+    `@Transactional`(readOnly = true)
     public PageDirectRoomResponseDto getRoomsForUser(Long currentUserId, Pageable pageable) {
         ...
-        java.util.ListLong opponentIds = rooms.stream()
+        ListLong opponentIds = rooms.stream()
             .map(room - room.getSender().getId().equals(currentUserId) ? room.getReceiver().getId() : room.getSender().getId())
+            .distinct()
             .toList();
```

## `rejectOtherPendingRequests` 의 락/경합 검토 권장

### 문제 파일 및 라인 5

- 파일: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java (2)`
- 라인: `137-152`

### 문제 5

- `findByItemIdAndStatus` 가 PENDING 요청들을 일반 SELECT 로 조회한 뒤 `updateStatus(REJECTED)` 로 일괄 변경합니다.
- 동시에 다른 트랜잭션이 같은 아이템에 대해 새로운 `ExchangeRequest` 를 PENDING 으로 생성하거나 다른 receiver 가 동시에 수락 처리를 시도하는 경우, 일부 PENDING 요청이 자동 거절 대상에서 누락되거나 `ACCEPTED` 가 두 건 이상 만들어질 수 있습니다.
- 실제 운영에서 동일 아이템의 receiver 는 한 명이므로 충돌 빈도는 낮겠지만, `Item` 단위로 비관적 락을 걸거나(또는 `UPDATE ... WHERE status=PENDING` 형태의 단건 SQL) 안전망을 두는 편이 권장됩니다.
