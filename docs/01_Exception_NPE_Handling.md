# PR Comments

## `updateRequestStatus` 가 비-ACCEPTED 분기에서 `null` 을 반환합니다 — 호출 측 NPE 위험

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java (1)`
- 라인: `96-135`

### 문제 1

- REJECTED 분기에서는 `return null`(L134)이 그대로 노출되며, 그 외의 상태가 들어왔을 때도 마찬가지로 `null` 이 반환됩니다.
- 컨트롤러나 다른 호출자가 `ApiResponse.success(...)` 등으로 그대로 감쌀 경우 응답 직렬화 단계에서 NPE가 발생하거나, 클라이언트가 채팅방 정보를 기대하는 응답에서 `null` 페이로드를 받게 됩니다.
- REJECTED 의 경우는 의도적으로 채팅방을 생성하지 않으므로 별도의 응답 DTO(예: 상태만 담긴) 를 반환하거나, 컨트롤러에서 분기 처리가 가능하도록 메서드 시그니처/반환 정책을 명시적으로 정리해 주세요.

## `targetUserIds`가 null이면 NPE 발생

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/service/UserConnectService.java`
- 라인: `42-47`

### 문제 2

- 44번 라인에서 `targetUserIds.size()`를 먼저 호출한 뒤 registry에 위임하고 있는데, 위임 대상인 `RedisUserSessionRegistry.areUsersConnected`는 null/empty를 모두 안전하게 처리하지만 이 메서드는 그 이전에 NPE를 던지게 됩니다.
- 호출부가 보장한다고 가정하더라도, 두 레이어 중 한 쪽만 가드를 두는 비대칭은 후속 리팩토링 시 사고 원인이 되기 쉽습니다.

### 제안 수정 2

```diff
-    public java.util.MapLong, Boolean getOnlineStatuses(java.util.ListLong targetUserIds) {
-        log.debug("[getOnlineStatuses] 접속 여부 일괄 조회 요청 - 대상 수: {}", targetUserIds.size());
-        // hasChatRoomBetween 조회를 생략 (호출부가 이미 방 목록 기반이므로)
-        return userSessionRegistry.areUsersConnected(targetUserIds);
-    }
+    public MapLong, Boolean getOnlineStatuses(ListLong targetUserIds) {
+        int size = targetUserIds == null ? 0 : targetUserIds.size();
+        log.debug("[getOnlineStatuses] 접속 여부 일괄 조회 요청 - 대상 수: {}", size);
+        // hasChatRoomBetween 조회를 생략 (호출부가 이미 방 목록 기반이므로)
+        return userSessionRegistry.areUsersConnected(targetUserIds);
+    }
```

(상단에 `java.util.Map`, `java.util.List` import 추가 권장)

## JSON 파싱 실패 시 fallback 동작이 가비지 파일명을 등록할 수 있음

### 문제 파일 및 라인 3

- 파일: `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
- 라인: `126-135`

### 문제 3

- `imageField`가 `[`로 시작했지만 JSON 파싱에 실패한 경우, fallback으로 `extractFileName(imageField, fileNames)`를 호출해 원본 문자열(예: `["http://.../a.jpg"`)을 그대로 파일명 추출 함수에 넘깁니다.
- `extractFileName`은 마지막 `/` 뒷부분을 잘라 Set에 넣으므로 `a.jpg"`처럼 따옴표/대괄호가 섞인 파일명이 referencedFileNames에 들어갈 수 있고, 이는 실제 디스크의 `a.jpg`와 매칭되지 않아 정상 파일이 고아로 오판될 위험이 있습니다.
- JSON 파싱 실패 시 경고 로그 후 해당 항목은 skip하거나, 콤마 분리 폴백을 적용하는 편이 안전합니다.

## `sendNotificationSafely` 의 catch 정책이 `ExchangeRequestService.sendNotificationToSender` 와 불일치

### 문제 파일 및 라인 4

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java (4)`
- 라인: `284-302`

### 문제 4

- `ExchangeRequestService.sendNotificationToSender`(L154-172)는 `FirebaseException` 만 좁게 catch 하고 그 외는 그대로 전파시키는 정책인 반면, 이쪽은 `FirebaseException` 외에 일반 `Exception` 까지 catch 해서 모두 흡수합니다.
- 두 정책 중 하나로 통일하는 편이 운영 시 트러블슈팅이 일관됩니다.
- 또한 `afterCommit` 안에서 던져지는 예외는 트랜잭션 커밋에 영향을 주지 않으므로 여기서 일반 Exception 까지 흡수해야 할 사유가 사실상 없습니다
  - 메인 비즈니스 로직 보호 목적이라면 `sendNotificationToSender` 와 동일하게 `FirebaseException` 만으로 충분.

## 직렬화 throw / 역직렬화 fallback 정책 LGTM

### 문제 파일 및 라인 5

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java (4)`
- 라인: `262-282`

### 문제 5

- 직렬화 실패 시 `InvalidRequestException` 을 던져 컨트롤러 롤백(`deleteImages`) 경로로 흘러가게 한 점, 역직렬화는 빈 리스트 fallback 으로 사용자 경험을 보호한 점 모두 이전 리뷰 의견을 정확히 반영했습니다.
- 다만 L279 의 역직렬화 실패 로그는 정상 운영에서는 거의 발생하지 않는 데이터 손상 시그널이므로 `log.error("...", e)` 로 stack trace 까지 남기는 편을 권장드립니다(현재는 `e.getMessage()` 만 남깁니다).
