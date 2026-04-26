# PR Comments

## `findAllImageUrls`는 결과량 무제한 — 향후 데이터가 커지면 메모리 부담

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java (1)`
- 라인: `82-84`

### 문제 1

- 스케줄러에서 한 번에 모든 `item.image` 문자열을 메모리로 로드하는 구조라, 운영 환경에서 아이템 수가 늘어나면 GC 압박과 일시적 힙 사용량 증가가 우려됩니다.
- 단기적으로는 일 1회 새벽 실행이라 허용 범위지만, 향후에는 `Stream`(Hibernate streaming) 또는 페이징 기반으로 처리하는 것을 권장합니다.

## `findMessagesAfterTimestamp` 결과량 무제한 — Pageable 또는 limit 도입 권장

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/repository/DirectMessageRepository.java (1)`
- 라인: `49-58`

### 문제 2

- `afterTimestamp` 이후의 메시지를 모두 fetch+JOIN FETCH로 가져오는 구조라, 사용자가 오랜만에 접속해 다량의 미수신 메시지가 쌓여 있는 경우 한 번의 쿼리로 매우 큰 결과셋을 메모리에 로드할 수 있습니다.
- `Pageable` 적용 또는 명시적 `LIMIT`(예: 최근 N건만) 도입을 권장합니다.
- 또한 `java.time.LocalDateTime`은 인라인 FQN 대신 import로 정리하는 편이 일관성에 좋습니다.

## 시간대(Timezone)와 결과 크기에 대한 보강 검토

### 문제 파일 및 라인 3

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/controller/DirectRoomController.java (2)`
- 라인: `72-80`

### 문제 3

- 두 가지 운영 관점 의견입니다.
  1. `LocalDateTime` 은 timezone-naive 타입이라 클라이언트와 서버가 다른 타임존에서 동작할 경우 누락 메시지 조회 결과가 어긋날 수 있습니다. `OffsetDateTime` / `Instant` 또는 ISO8601(zoned) 문자열 기반으로 받는 편이 안전합니다.
  2. `afterTimestamp` 가 과거의 매우 오래된 시점일 경우 응답 크기가 무제한으로 커질 수 있습니다. 페이지네이션 또는 최대 반환 개수 상한을 두는 것을 권장드립니다.

## 주석의 "in-memory 연산" 표현이 사실과 다름 + N회 Redis 왕복 발생

### 문제 파일 및 라인 4

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/registry/RedisUserSessionRegistry.java (2)`
- 라인: `66-78`

### 문제 4

- `isUserConnected`는 매 호출마다 `redisTemplate.hasKey(...)`를 통해 Redis로 네트워크 왕복을 합니다.
- 따라서 `for` 루프는 in-memory 연산이 아니라 N번의 Redis I/O이며, 채팅방 목록이 커질수록 latency가 선형으로 증가합니다.
- 주석 문구를 수정하고, 호출 규모가 커질 가능성이 있다면 `executePipelined`로 일괄 조회하는 것을 권장합니다.

### 파이프라인 적용 예시 4

```diff
-    public java.util.MapLong, Boolean areUsersConnected(java.util.ListLong userIds) {
-        java.util.MapLong, Boolean result = new java.util.HashMap();
-        if (userIds == null || userIds.isEmpty()) return result;
-
-        // 파이프라인 대신 간단히 여러 키의 존재 여부를 순회 검사
-        // in-memory 연산이므로 50건 미만의 N은 성능 이슈 거의 없음
-        for (Long userId : userIds) {
-            result.put(userId, isUserConnected(userId));
-        }
-
-        return result;
-    }
+    public MapLong, Boolean areUsersConnected(ListLong userIds) {
+        MapLong, Boolean result = new HashMap();
+        if (userIds == null || userIds.isEmpty()) return result;
+
+        ListObject exists = redisTemplate.executePipelined((RedisCallbackObject) connection - {
+            StringRedisConnection conn = (StringRedisConnection) connection;
+            for (Long userId : userIds) {
+                conn.exists(USER_SESSION_KEY_PREFIX + userId);
+            }
+            return null;
+        });
+        for (int i = 0; i  userIds.size(); i++) {
+            result.put(userIds.get(i), Boolean.TRUE.equals(exists.get(i)));
+        }
+        return result;
+    }
```
