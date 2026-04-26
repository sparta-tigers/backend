# PR Comments

## CANCEL 분기에서 시스템 상태 메시지가 절대 발행되지 않는 로직 버그

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- 라인: `152-160`

### 문제 1

- `updateStatus(REJECTED)` 가 먼저 호출(L153)된 뒤 `req.getStatus() == ExchangeStatus.ACCEPTED`(L156)를 검사하기 때문에 이 조건은 **항상 false** 입니다.
- 결과적으로 ACCEPTED → REJECTED 로 전이되는 요청에 연결된 채팅방에 `STATUS_UPDATED` 시스템 메시지가 전혀 발행되지 않습니다.
- 클라이언트는 채팅방이 갑자기 disabled 되는 상황을 실시간으로 감지하지 못하고, "잠시 전까지 메시지를 보내던 방"이 다음 새로고침 시점까지 정상처럼 보이게 됩니다.

### 제안 패치 1

```diff
                 for (ExchangeRequest req : activeRequests) {
+                    ExchangeStatus previousStatus = req.getStatus();
                     req.updateStatus(ExchangeStatus.REJECTED);
                     // PENDING 및 ACCEPTED 상태였던 사용자들에게 교환 취소(재오픈) 알림 발송
                     sendNotificationSafely(req, "교환 취소", "상대방의 사정으로 교환이 취소되었습니다.");
-                    if (req.getStatus() == ExchangeStatus.ACCEPTED) {
+                    if (previousStatus == ExchangeStatus.ACCEPTED) {
                         directRoomRepository.findByExchangeRequestId(req.getId())
                             .ifPresent(room - publishSystemStatusUpdatedMessage(room.getId()));
                     }
                 }
```

## 거절(REJECTED)과 완료(COMPLETED)를 하나의 코드로 묶는 것이 의도된 것인지 확인 필요

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/global/exception/enums/ExceptionCode.java (1)`
- 라인: `71-71`

### 문제 2

- `EXCHANGE_NOT_ACCEPTED_REJECTED`의 메시지는 "거절되거나 완료된 교환입니다."로 두 상태를 한 코드에 합치고 있습니다.
- PR 설명에는 "비수락 상태에 대해 distinct 에러 코드를 추가했다"고 되어 있는데, 클라이언트 입장에서 두 상태를 분리해 다뤄야 하는 케이스(예: 완료 건은 후속 동선 노출 vs 거절 건은 재요청 유도)가 있다면 별도 코드로 분리하는 편이 향후 UI/로깅 처리에 유리합니다.
- 의도라면 그대로 두어도 무방합니다.

## ACCEPTED 게이트 추가 — 좋습니다. 다만 COMPLETED 분기 메시지가 모호해질 수 있습니다

### 문제 파일 및 라인 3

- 파일: `src/main/java/com/sparta/spartatigers/domain/stompchat/service/ExchangeChatService.java (1)`
- 라인: `71-82`

### 문제 3

- PENDING/REJECTED 를 구분해서 사유 코드를 분기한 처리는 이전 리뷰(FORBIDDEN_REQUEST 일반화 문제)를 잘 반영했습니다.
- 한 가지 보완 의견은 `exchangeStatus` 가 `COMPLETED` 인 경우입니다.
- 정상 흐름에서는 직전 L66 의 `room.isCompleted()` 체크가 먼저 차단하지만, `DirectRoom.isCompleted` 와 `ExchangeRequest.status` 가 일시적으로 어긋나는 상황(예: 마이그레이션, 일부 경로에서 `room.complete()` 누락)에서는 `EXCHANGE_NOT_ACCEPTED_REJECTED` 가 잘못 노출될 수 있습니다.
- `switch (exchangeStatus)` 로 PENDING/REJECTED/COMPLETED 를 명시적으로 매핑하거나, 최소한 default 케이스에서 사유를 별도 코드로 두는 편을 권장드립니다.

## `getMessagesAfter` 권한 체크 방식 일관성 검토 권장

### 문제 파일 및 라인 4

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java (3)`
- 라인: `198-218`

### 문제 4

- 이 메서드는 `room.getSender()` / `room.getReceiver()` 기준으로 권한을 검증하고 있는데, 같은 파일의 `getRoomItem`(L119-120) 은 `exchangeRequest.getSender/getReceiver` 기준으로 통일한 변경을 했습니다.
- PR의 일관성 관점에서 두 메서드 모두 동일한 기준(exchangeRequest 기준)을 사용하도록 맞추는 것을 권장드립니다.
