# Phase 2: Support 계층 구축

> **목표:** 비즈니스를 보조하는 기술 및 외부 연동 인프라 컴포넌트를 `domain/support/` 하위로 이동한다.
> **핵심 원칙:** support 패키지에는 비즈니스 로직이 없어야 한다. support → core 역참조는 절대 금지.

---

## 2-1. chat (stompchat → `support/chat/`)

### 이동 대상

| 현재 경로 | 이동 경로 | 비고 |
|---|---|---|
| `stompchat/interceptor/StompInterceptor.java` | `support/chat/interceptor/` | STOMP 인프라 |
| `stompchat/interceptor/StompPrincipal.java` | `support/chat/interceptor/` | |
| `stompchat/eventlistener/WebSocketEventListener.java` | `support/chat/eventlistener/` | WS 연결/해제 이벤트 |
| `stompchat/model/ChatDomainType.java` | `support/chat/model/` | Enum |
| `stompchat/model/ChatMessage.java` | `support/chat/model/` | 채팅 메시지 엔티티 |
| `stompchat/pubsub/RedisChatPublisher.java` | `support/chat/pubsub/` | Redis Pub/Sub 인프라 |
| `stompchat/pubsub/RedisChatSubscriber.java` | `support/chat/pubsub/` | |
| `stompchat/pubsub/RedisLocationPublisher.java` | `support/chat/pubsub/` | |
| `stompchat/pubsub/RedisLocationSubscriber.java` | `support/chat/pubsub/` | |
| `stompchat/repository/LiveBoardChatRepository.java` | `support/chat/repository/` | 채팅 저장소 |
| `stompchat/service/ChatService.java` | `support/chat/service/` | 범용 채팅 서비스 |
| `stompchat/controller/LiveBoardChatController.java` | `support/chat/controller/` | 라이브보드 채팅 (foundation 참조만 → 허용) |
| `stompchat/controller/LocationController.java` | `support/chat/controller/` | |
| `stompchat/dto/request/LocationRequestDto.java` | `support/chat/dto/request/` | |
| `stompchat/dto/response/RedisUpdateDto.java` | `support/chat/dto/response/` | |
| `stompchat/service/LocationService.java` | `support/chat/service/` | 위치 기반 알림 |

**다른 도메인에서 이관된 파일:**

| 현재 경로 | 이동 경로 | 비고 |
|---|---|---|
| `liveboard/pubsub/LiveBoardMatchSubscriber.java` | `support/chat/pubsub/` | Redis 실시간 경기 데이터 수신 (Phase 1) |
| `directRoom/registry/RedisUserSessionRegistry.java` | `support/chat/registry/` | WebSocket 세션 트래커 인프라 |

### 핵심 문제: support → core 역참조 제거

현재 `stompchat`이 core 도메인을 직접 참조하는 코드:

```
stompchat → directRoom (core/direct):
  - ExchangeChatController → ChatMessageRequest, DirectMessageService, 
    RedisUserSessionRegistry, DirectMessage, DirectRoom, 
    DirectRoomRepository, DirectMessageRepository, ChatMessageResponse, RedisMessage
  - ExchangeChatService → (directRoom 관련 로직)

stompchat → exchangerequest (core/trade):
  - ExchangeStatus 참조

stompchat/pubsub:
  - RedisDirectMessagePublisher.java → directRoom 타입 참조
  - RedisDirectMessageSubscriber.java → directRoom 타입 참조
```

### 해결 전략

#### 방안 A: 비즈니스 컨트롤러를 core로 이동 (권장)

| 파일 | 처리 |
|---|---|
| `ExchangeChatController.java` | → `core/direct/controller/`로 이동 (DM 비즈니스 로직) |
| `ExchangeChatService.java` | → `core/direct/service/`로 이동 |
| `RedisDirectMessagePublisher.java` | → `core/direct/pubsub/`로 이동 (DM 전용 인프라) |
| `RedisDirectMessageSubscriber.java` | → `core/direct/pubsub/`로 이동 |

> **원칙:** "교환 채팅"의 메시지 전송/수신 비즈니스 로직은 `core/direct`의 책임이다.
> `support/chat`에는 순수 STOMP/Redis 인프라와 라이브보드 채팅(foundation 참조만)만 남긴다.

#### LiveBoardMatchSubscriber DIP 적용

`LiveBoardMatchSubscriber`가 `foundation/baseball/match`의 서비스를 직접 호출하면 support → foundation 의존이 발생한다 (support → foundation은 허용). 따라서 직접 호출이 가능하지만, 더 깔끔한 설계를 위해:

1. `foundation/baseball/match/` 에 `LiveBoardDataReceiver` 인터페이스를 정의
2. `LiveBoardMatchService`가 이를 구현
3. `support/chat/pubsub/LiveBoardMatchSubscriber`가 인터페이스를 통해 데이터 전달

> support → foundation 의존은 허용이므로 DIP 없이 직접 호출해도 룰 위반은 아니다.
> 다만 향후 MSA 분리를 고려하면 인터페이스 분리가 유리하다.

---

## 2-2. notification → ✘ (PR#68에서 완료됨 — 이 섹션은 건너뜀)

> PR#68 (`[feat/notification] FCM 기반 알림 기능 구현`)에서 다음 변경이 이미 반영됨:
> - `domain/notification/` 패키지 **전체 삭제** (DeviceTokenController, DeviceTokenRequest, DeviceTokenService)
> - `global/firebase/FCMService.java` → `global/firebase/service/FCMService.java`로 이동 완료
> - 신규: `global/firebase/controller/FCMController.java`, `global/firebase/dto/FcmTokenRequest.java`, `global/firebase/dto/NotificationMessage.java`, `global/firebase/service/NotificationService.java`
> - FCM 토큰 등록 기능이 `UserService.updateFcmToken()`으로 이동됨
>
> **따라서 Phase 2에서 notification 관련 작업은 없음. `global/firebase/`는 현행 유지.**

---

## 2-3. image → `support/image/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/image/controller/ImageController.java` | `domain/support/image/controller/` |
| `domain/image/service/ImageStorageService.java` | `domain/support/image/service/` |
| `domain/image/service/LocalImageStorageServiceImpl.java` | `domain/support/image/service/` |

### 역참조 문제: OrphanImageCleanupScheduler → ItemRepository (core/trade)

현재 `OrphanImageCleanupScheduler`가 `ItemRepository`를 import하여 고아 이미지를 정리한다. 이는 support → core 역참조.

**해결:**
1. `core/trade`에서 사용 중인 이미지 ID 목록을 Event 또는 인터페이스로 노출
2. 또는 `OrphanImageCleanupScheduler` 자체를 `core/trade/scheduler/`로 이동 (이미지 정리가 사실상 trade의 비즈니스 보조 로직이므로)
3. 가장 단순한 방법: `ImageStorageService`에 "사용 중 확인" 메서드를 두고, core/trade가 호출하도록 방향을 뒤집는다

> **권장: 방안 2** — `OrphanImageCleanupScheduler`를 `core/trade/scheduler/`로 이동. 이 스케줄러는 "아이템 이미지 정리"라는 trade 도메인의 부가 기능이다.

**import 치환:**
```
com.sparta.spartatigers.domain.image.
→ com.sparta.spartatigers.domain.support.image.
```

---

## 2-4. weather → `support/weather/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/weather/` (전체 16 files) | `domain/support/weather/` |

> weather는 외부 기상청 API와의 순수 연동 모듈이므로 support에 적합.
> weather → foundation 참조 없음 (독립적).

**import 치환:**
```
com.sparta.spartatigers.domain.weather.
→ com.sparta.spartatigers.domain.support.weather.
```

---

## 2-5. global/config 수정

Support 이동으로 인해 global/config에서 경로 갱신이 필요한 파일:

| Config 파일 | 변경 사항 |
|---|---|
| `WebSocketConfig.java` | `StompInterceptor` import 경로 → `support.chat.interceptor` |
| `RedisConfig.java` | `RedisLocationSubscriber` import 경로 변경 (`RedisDirectMessageSubscriber`는 Phase 3에서 변경) |

---

## 2-6. Phase 2 완료 후 support 패키지 구조

```
domain/support/
├── chat/
│   ├── controller/LiveBoardChatController.java, LocationController.java
│   ├── dto/request/LocationRequestDto.java
│   ├── dto/response/RedisUpdateDto.java
│   ├── eventlistener/WebSocketEventListener.java
│   ├── interceptor/StompInterceptor.java, StompPrincipal.java
│   ├── model/ChatDomainType.java, ChatMessage.java
│   ├── pubsub/RedisChatPublisher.java, RedisChatSubscriber.java,
│   │        RedisLocationPublisher.java, RedisLocationSubscriber.java,
│   │        LiveBoardMatchSubscriber.java
│   ├── registry/RedisUserSessionRegistry.java
│   ├── repository/LiveBoardChatRepository.java
│   └── service/ChatService.java, LocationService.java
│
├── image/
│   ├── controller/ImageController.java
│   └── service/ImageStorageService.java, LocalImageStorageServiceImpl.java
│
└── weather/
    ├── api/ApiTimeCalculator.java, WeatherApiUrlGenerator.java
    ├── controller/WeatherController.java
    ├── dto/ForeCastResponseDto.java, NowCastResponseDto.java, WeatherBundle.java
    ├── model/RainType.java, SkyStatus.java, Weather.java, WeatherApiStatus.java, WeatherStadium.java, WindDirection.java
    ├── response/OriginResponse.java
    ├── service/WeatherService.java
    └── util/LatLonToGrid.java, WeatherParser.java
```

---

## 2-7. Phase 2 검증 체크리스트

- [ ] `scripts/verify.sh` 실행 → 컴파일 성공
- [ ] 전체 테스트 Green
- [ ] `support/` 패키지에서 `core/` 패키지를 import하는 코드가 **0건**인지 grep 확인:
  ```bash
  grep -r "domain.core\." src/main/java/com/sparta/spartatigers/domain/support/ --include="*.java" | wc -l
  # 결과: 0 이어야 함
  ```
- [ ] 원본 디렉토리 삭제 확인 (`domain/weather/`)
- [ ] `domain/notification/` 디렉토리는 PR#68에서 이미 삭제됨 — 잔존 여부만 확인
- [ ] `domain/stompchat/`과 `domain/image/` 디렉토리는 Phase 3 이동 대상 파일이 남아있으므로 **삭제하지 않고 유지**
- [ ] `global/firebase/` 패키지는 PR#68에서 재편됨 — 현행 유지 (이동 없음)
- [ ] ExchangeChatController, ExchangeChatService, RedisDirectMessage* → core/direct 이동 완료 (Phase 3에서 수행)
- [ ] `git tag phase-2-done`
