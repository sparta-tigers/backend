# Phase 3: Core 계층 구축 및 이벤트 기반 느슨한 결합

> **목표:** 서비스의 4대 핵심 비즈니스 컴포넌트를 `domain/core/` 하위로 이동하고, Core 간 직접 참조를 Spring Event로 전환한다.
> **핵심 룰:** Core 도메인 간 `@Autowired`/생성자 직접 주입 절대 금지. 반드시 `ApplicationEventPublisher` + `@TransactionalEventListener`로 우회.

---

## 3-1. attendance (matchAttendance → `core/attendance/`)

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/matchAttendance/controller/MatchAttendanceController.java` | `domain/core/attendance/controller/` |
| `domain/matchAttendance/controller/OcrTestController.java` | `domain/core/attendance/controller/` |
| `domain/matchAttendance/dto/` (4 files) | `domain/core/attendance/dto/` |
| `domain/matchAttendance/model/` (3 files) | `domain/core/attendance/model/` |
| `domain/matchAttendance/repository/MatchAttendanceRepository.java` | `domain/core/attendance/repository/` |
| `domain/matchAttendance/service/MatchAttendanceService.java` | `domain/core/attendance/service/` |
| `domain/matchAttendance/service/OcrService.java` | `domain/core/attendance/service/` |

**의존성 분석:**
- `attendance → foundation/user` (User, UserRepository) ✅ 허용
- `attendance → foundation/baseball/match` (Match) ✅ 허용
- `attendance → support/image` (ImageStorageService) ✅ 허용
- attendance → 다른 core 도메인 참조 없음 → **의존성 위반 없음**

**import 치환:**
```
com.sparta.spartatigers.domain.matchAttendance.
→ com.sparta.spartatigers.domain.core.attendance.
```

---

## 3-2. trade (item + exchangerequest → `core/trade/`)

### 파일 이동

#### item → `core/trade/` (아이템 부분)

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/item/controller/ItemController.java` | `domain/core/trade/controller/` |
| `domain/item/dto/request/` (5 files) | `domain/core/trade/dto/request/` |
| `domain/item/dto/response/` (3 files) | `domain/core/trade/dto/response/` |
| `domain/item/event/ItemLocationUpdatedEvent.java` | `domain/core/trade/event/` |
| `domain/item/model/Item.java` | `domain/core/trade/model/` |
| `domain/item/model/ItemCategory.java` | `domain/core/trade/model/` |
| `domain/item/model/ItemStatus.java` | `domain/core/trade/model/` |
| `domain/item/repository/ItemRepository.java` | `domain/core/trade/repository/` |
| `domain/item/service/ItemService.java` | `domain/core/trade/service/` |

#### exchangerequest → `core/trade/` (교환 요청 부분)

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/exchangerequest/controller/ExchangeRequestController.java` | `domain/core/trade/controller/` |
| `domain/exchangerequest/dto/request/` (2 files) | `domain/core/trade/dto/request/` |
| `domain/exchangerequest/dto/response/` (3 files) | `domain/core/trade/dto/response/` |
| `domain/exchangerequest/model/ExchangeRequest.java` | `domain/core/trade/model/` |
| `domain/exchangerequest/model/ExchangeStatus.java` | `domain/core/trade/model/` |
| `domain/exchangerequest/repository/ExchangeRequestRepository.java` | `domain/core/trade/repository/` |
| `domain/exchangerequest/service/ExchangeRequestService.java` | `domain/core/trade/service/` |

#### Phase 2에서 지연된 파일 수용

| 출처 | 이동 경로 | 비고 |
|---|---|---|
| `image/scheduler/OrphanImageCleanupScheduler.java` | `core/trade/scheduler/` | 아이템 이미지 정리 |
| `item/event/ItemLocationEventListener.java` | `support/chat/event/` | 위치 알림 리스너 (support → core 역참조 해소) |

### 핵심 의존성 제거: trade → direct 직접 참조

**현재 위반 코드 (ItemService):**
```java
// ❌ core/trade → core/direct 직접 참조 (금지)
import ...directRoom.repository.DirectRoomRepository;
import ...stompchat.pubsub.RedisDirectMessagePublisher;
```

**해결: ItemStatusChangedEvent 패턴**

1. **이벤트 정의** (`foundation/common/event/ItemStatusChangedEvent.java`):
   - 최소 페이로드: `exchangeRequestId`, `message`
   - `Yagu-Loose-Coupling-Event-Pattern` 준수: ID와 필요한 문자열만 포함

2. **발행 (Publisher)** — `ItemService`:
   - 교환 상태 변경(ACCEPTED/COMPLETED 등) 시 `applicationEventPublisher.publishEvent(new ItemStatusChangedEvent(...))`
   - 기존 `DirectRoomRepository` 등을 이용하던 코드를 리스너로 위임

3. **구독 (Listener)** — `core/direct/event/ItemStatusChangedEventListener.java`:
   - `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
   - `@Async` 적용
   - `DirectRoom` 상태 변경 및 시스템 메시지 발행 수행

**적용 후 의존성 흐름:**
```
trade ──(event)──→ direct
(직접 import 없음, ApplicationEvent를 통한 느슨한 결합)
```

### 핵심 의존성 제거: support/chat(LocationService) → core/trade 역참조 해소

**현재 위반 코드:**
- `LocationService`가 `item.model.Item` 엔티티를 import (❌ support → core 역참조)
- (`ItemService`는 이미 `ItemLocationUpdatedEvent` 이벤트 패턴이 적용되어 `LocationService`를 직접 호출하지 않음 — core → support 결합은 없음)

**해결:**
- `ItemLocationEventListener`를 `domain/item/event/`에서 `support/chat/event/`로 이동. support가 trade의 이벤트를 구독하여 위치 알림을 전송하는 흐름.
- `LocationService` 메서드의 파라미터에서 `Item` 엔티티 의존성을 제거하고, `Event` 객체로부터 순수 데이터(`itemId`, `latitude`, `longitude` 등)만 전달받도록 시그니처 수정.

---

## 3-3. direct (directRoom → `core/direct/`)

### 파일 이동

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/directRoom/controller/DirectMessageController.java` | `domain/core/direct/controller/` |
| `domain/directRoom/controller/DirectRoomController.java` | `domain/core/direct/controller/` |
| `domain/directRoom/controller/UserConnectController.java` | `domain/core/direct/controller/` |
| `domain/directRoom/dto/request/` (3 files) | `domain/core/direct/dto/request/` |
| `domain/directRoom/dto/response/` (6 files) | `domain/core/direct/dto/response/` |
| `domain/directRoom/model/DirectMessage.java` | `domain/core/direct/model/` |
| `domain/directRoom/model/DirectRoom.java` | `domain/core/direct/model/` |
| `domain/directRoom/repository/` (2 files) | `domain/core/direct/repository/` |
| `domain/directRoom/service/` (3 files) | `domain/core/direct/service/` |

**Phase 2에서 수용하는 파일:**

| 출처 | 이동 경로 |
|---|---|
| `stompchat/controller/ExchangeChatController.java` | `core/direct/controller/` |
| `stompchat/service/ExchangeChatService.java` | `core/direct/service/` |
| `stompchat/pubsub/RedisDirectMessagePublisher.java` | `core/direct/pubsub/` |
| `stompchat/pubsub/RedisDirectMessageSubscriber.java` | `core/direct/pubsub/` |

**신규 생성:**

| 파일 | 역할 |
|---|---|
| `core/direct/event/ItemStatusChangedEventListener.java` | 교환 상태 변경 이벤트 구독 → 채팅방 상태 갱신 및 알림 전송 |

### 핵심 의존성 제거: direct → trade 역참조

**현재 위반 코드 (DirectRoomService 또는 관련 파일):**
```java
// ❌ core/direct → core/trade 직접 참조
import ...exchangerequest.model.ExchangeRequest;
import ...exchangerequest.repository.ExchangeRequestRepository;
import ...item.model.Item;
```

**해결:**
- `DirectRoomService` 및 `ExchangeChatService`에서 `ExchangeRequestRepository` 의존성 주입 제거
- `core/direct/repository/TradeQueryDao.java` (JdbcTemplate 사용) 신규 생성
- 프론트엔드 응답(DTO) 스펙(상대방 정보, 아이템 정보 등)을 유지하기 위해 CQRS 패턴을 적용하여, Entity 참조 없이 SQL로 필요한 데이터만 읽기 전용으로 조회
- 이를 통해 사이드이펙트 제로(0)로 백엔드 결합도 제거 달성

---

## 3-4. ticketalarm → `core/ticketalarm/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/ticketalarm/` (전체 11 files) | `domain/core/ticketalarm/` |

**의존성 분석:**
- `ticketalarm → foundation/user` (User, UserRepository) ✅ 허용
- `ticketalarm → foundation/baseball/team` (Team) ✅ 허용
- ticketalarm → 다른 core 도메인 참조 없음 → **의존성 위반 없음**

**import 치환:**
```
com.sparta.spartatigers.domain.ticketalarm.
→ com.sparta.spartatigers.domain.core.ticketalarm.
```

---

## 3-5. 이벤트 정리 요약

Phase 3 완료 후 프로젝트에 존재해야 할 이벤트 목록:

| 이벤트 | 발행 위치 | 구독 위치 | 트리거 |
|---|---|---|---|
| `ItemLocationUpdatedEvent` | `core/trade/service/ItemService` | `support/chat/event/ItemLocationEventListener` | 아이템 CRUD 시 주변 유저 알림 |
| `ItemStatusChangedEvent` | `core/trade/service/ItemService` | `core/direct/event/ItemStatusChangedEventListener` | 아이템 상태 변경 → 채팅방 갱신 및 알림 |

### 이벤트 설계 원칙 (Yagu-Loose-Coupling-Event-Pattern)

```java
// ✅ 이벤트 객체: 최소한의 정보만 포함
public record ItemStatusChangedEvent(
    Long exchangeRequestId,
    String message
) {}

// ✅ 리스너: AFTER_COMMIT + @Async
@Component
@RequiredArgsConstructor
public class ItemStatusChangedEventListener {
    // 필요한 의존성 주입 (DirectRoomRepository 등)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItemStatusChanged(ItemStatusChangedEvent event) {
        // 채팅방 상태 갱신 및 메시지 발송 로직
    }
}
```

> ⚠️ **위 코드는 설계 의도를 보여주기 위한 의사코드(pseudocode)입니다. 실제 구현은 Phase 실행 시 기존 코드를 분석하여 결정합니다.**

---

## 3-6. Phase 3 완료 후 import 치환 요약

| 이동 전 import 패턴 | 이동 후 import 패턴 |
|---|---|
| `domain.matchAttendance.` | `domain.core.attendance.` |
| `domain.item.` | `domain.core.trade.` |
| `domain.exchangerequest.` | `domain.core.trade.` |
| `domain.directRoom.` | `domain.core.direct.` |
| `domain.ticketalarm.` | `domain.core.ticketalarm.` |

---

## 3-7. global/config 수정 (지연 처리건)

Phase 3 이동으로 인해 global/config에서 경로 갱신이 최종적으로 필요한 파일:

| Config 파일 | 변경 사항 |
|---|---|
| `RedisConfig.java` | `RedisDirectMessageSubscriber` import 경로 → `core.direct.pubsub` |

---

## 3-8. Phase 3 검증 체크리스트

- [ ] `scripts/verify.sh` 실행 → 컴파일 성공
- [ ] 전체 테스트 Green
- [ ] Core 간 직접 참조가 **0건**인지 확인:
  ```bash
  # core/trade → core/direct 참조 확인
  grep -r "domain.core.direct\." src/main/java/com/sparta/spartatigers/domain/core/trade/ --include="*.java" | wc -l
  # 결과: 0

  # core/direct → core/trade 참조 확인
  grep -r "domain.core.trade\." src/main/java/com/sparta/spartatigers/domain/core/direct/ --include="*.java" | wc -l
  # 결과: 0

  # core/attendance → 다른 core 참조 확인
  grep -r "domain.core\." src/main/java/com/sparta/spartatigers/domain/core/attendance/ --include="*.java" | grep -v "domain.core.attendance" | wc -l
  # 결과: 0
  ```
- [ ] support → core 역참조가 **0건**인지 확인:
  ```bash
  grep -r "domain.core\." src/main/java/com/sparta/spartatigers/domain/support/ --include="*.java" | wc -l
  # 결과: 0
  ```
- [ ] 이벤트 발행/구독이 정상 동작하는지 통합 테스트 또는 수동 테스트
- [ ] `domain/matchAttendance/`, `domain/item/`, `domain/exchangerequest/`, `domain/directRoom/`, `domain/ticketalarm/` 원본 디렉토리 삭제 확인
- [ ] Phase 1~2에서 지연 삭제되었던 `domain/liveboard/`, `domain/stompchat/`, `domain/image/` 원본 디렉토리 최종 삭제 확인
- [ ] `git tag phase-3-done`
