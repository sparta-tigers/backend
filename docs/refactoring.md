# 백엔드 패키지 구조 개선

## 📂 1. 최종 변경될 패키지 구조

우리의 모든 도메인은 그 역할과 성격에 따라 **Foundation(기반), Core(핵심), Support(지원)** 3가지 큰 그룹으로 묶입니다.

```markdown
src/main/java/com/sparta/spartatigers/
├── global/                                 # ⚙️ 프레임워크 인프라 및 교차 관심사
│   ├── aop/                               # Auth, AuthArgumentResolver, OauthProviderConverter, TokenClaim(이관)
│   ├── config/                            # (현행 유지) 13개 Config 클래스
│   ├── exception/                         # (현행 유지) 전역 예외 처리
│   ├── firebase/                          # (PR#68 재편) FCM 알림 인프라
│   │   ├── controller/FCMController.java
│   │   ├── dto/FcmTokenRequest.java, NotificationMessage.java
│   │   └── service/FCMService.java, NotificationService.java
│   ├── notification/                      # (현행 유지) Discord 웹훅 등 시스템 알림
│   ├── response/                          # (현행 유지) ApiResponse, ErrorResponse 등 공통 응답
│   └── util/                              # (현행 유지) RedisRateLimiter 등 유틸리티
└── domain/
    ├── common/                             # 📦 도메인 공통 자원
    │   └── entity/BaseEntity.java
    │
    ├── foundation/                         # 🧱 [기반 도메인] 시스템의 뿌리가 되는 핵심 엔티티 및 메타
    │   ├── user/                           # 👤 사용자 도메인 영역
    │   │   ├── auth/                       # (기존 auth) 소셜 로그인 및 토큰 발급
    │   │   │   ├── client/KakaoClient.java
    │   │   │   ├── controller/AuthController.java
    │   │   │   ├── dto/KakaoTokenResponse.java, KakaoUserInfo, KakaoUserMeResponse, LogoutRequest, OauthLoginRequest, RefreshRequest, UserLoginRequest
    │   │   │   ├── model/OAuthProvider.java, Oauth.java, RefreshToken.java, Token.java
    │   │   │   ├── repository/OauthRepository.java, RefreshTokenRepository.java
    │   │   │   └── service/AuthService.java, BcryptPasswordEncoder.java, JwtTokenService.java, PasswordEncoder.java, TokenService.java
    │   │   │
    │   │   ├── account/                    # (기존 user) 기본 계정 정보 관리
    │   │   │   ├── controller/UserRestController.java
    │   │   │   ├── dto/UserRegisterRequest.java, UserResponseDto.java
    │   │   │   ├── model/LoginUser.java, User.java, UserRole.java
    │   │   │   ├── repository/UserRepository.java
    │   │   │   └── service/UserService.java
    │   │   │
    │   │   └── favoriteteam/               # (기존 favoriteteam) 유저 선호 구단 메타
    │   │       ├── controller/FavTeamController.java
    │   │       ├── dto/FavTeamRequestDto.java, FavTeamResponseDto.java
    │   │       ├── model/entity/FavoriteTeam.java
    │   │       ├── repository/FavTeamRepository.java
    │   │       └── service/FavTeamService.java
    │   │
    │   └── baseball/                       # ⚾ 야구 도메인 메타 영역
    │       ├── team/                       # (기존 team + liveboard 일부) 구단, 선수, 경기장 정보
    │       │   ├── model/Team.java, TeamCode.java, Stadium.java, Player.java (기존 liveboard의 Player 포함)
    │       │   └── repository/TeamRepository.java, StadiumRepository.java
    │       │
    │       ├── match/                      # (기존 liveboard 일부) 경기 일정 및 결과 데이터베이스
    │       │   ├── controller/MatchController.java, LiveBoardRoomController.java
    │       │   ├── dto/MatchScheduleResponseDto.java, LiveBoardDataResponseDto.java, LiveBoardRoomResponseDto.java
    │       │   ├── model/Match.java, MatchResult.java, MatchScore.java, InningTexts.java, HomeAway.java, LeagueType.java, LiveBoardStatus.java, LiveBoardConnection.java, LiveBoardData.java, LiveBoardRoom.java
    │       │   ├── repository/MatchRepository.java, LiveBoardConnectionRepository.java, LiveBoardRoomRepository.java
    │       │   ├── service/MatchScheduleService.java, LiveBoardDataService.java, LiveBoardMatchService.java, LiveboardRoomService.java
    │       │   └── util/GlobalSessionGenerator.java
    │       │
    │       ├── lineup/                     # (기존 startinglineup + liveboard 일부) 경기 라인업 엔티티
    │       │   ├── controller/LineupController.java
    │       │   ├── dto/LineupBatterResponse.java, LineupCacheDto.java, LineupResponseDto.java
    │       │   ├── model/LineupPlayer.java, Position.java, StartingLineup.java, StartingLineupPK.java, LineupBatter.java
    │       │   ├── repository/StartingLineupRepository.java
    │       │   └── service/StartingLineupService.java, LineupQueryService.java
    │       │
    │       ├── ranking/                    # (기존 ranking) 구단 순위 및 포스트시즌 통계
    │       │   ├── controller/TeamRankingController.java
    │       │   ├── dto/MatchDetailDto.java, PostSeasonResponseDto.java, PostseasonStage.java, TeamRankingResponseDto.java, TeamRankingStat.java
    │       │   ├── repository/TeamRankingRepositoryCustom.java, TeamRankingRepositoryCustomImpl.java
    │       │   └── service/TeamRankingService.java
    │       │
    │       └── home/                       # (기존 home + dashboard 합병) 홈 화면 디스플레이 정보 가공
    │           ├── controller/DashboardController.java (기존 dashboard 이동)
    │           ├── dto/HomeResponseDto.java, HomeDashboardResponseDto.java (기존 dashboard 이동)
    │           └── service/HomeService.java, HomeMatchScheduleService.java, HomeRankingService.java, HomeTodayGameService.java, DashboardService.java (기존 dashboard 이동)
    │
    ├── core/                               # 🎯 [핵심 비즈니스] 서비스의 주가 되는 4대 독립적 비즈니스 컴포넌트
    │   ├── attendance/                     # (기존 matchAttendance) 경기 직관 인증 및 OCR 검증
    │   │   ├── controller/MatchAttendanceController.java, OcrTestController.java
    │   │   ├── dto/MatchAttendanceRequestDto.java, MatchAttendanceResponseDto.java, MatchAttendanceUpdateRequestDto.java, TicketOcrResponseDto.java
    │   │   ├── model/AttendanceImage.java, AttendanceImageType.java, MatchAttendance.java
    │   │   ├── repository/MatchAttendanceRepository.java
    │   │   └── service/MatchAttendanceService.java, OcrService.java
    │   │
    │   ├── trade/                          # (기존 item + exchangerequest 합병) 거래 아이템 등록 및 교환 비즈니스
    │   │   ├── controller/ItemController.java, ExchangeRequestController.java
    │   │   ├── dto/
    │   │   │   ├── request/ItemAction.java, ItemCreateRequest.java, FindItemByIdRequestDto.java, UpdateItemRequestDto.java, UpdateItemStatusRequestDto.java, ExchangeRequestDto.java, UpdateExchangeRequestDto.java
    │   │   │   └── response/ItemResponseDto.java, ReadItemDetailResponseDto.java, ReadItemResponseDto.java, ExchangeRoomResponseDto.java, ReceiveRequestResponseDto.java, SendRequestResponseDto.java
    │   │   ├── event/ItemLocationUpdatedEvent.java, TradeAcceptedEvent.java
    │   │   ├── scheduler/OrphanImageCleanupScheduler.java
    │   │   ├── model/Item.java, ItemCategory.java, ItemStatus.java, ExchangeRequest.java, ExchangeStatus.java
    │   │   ├── repository/ItemRepository.java, ExchangeRequestRepository.java
    │   │   └── service/ItemService.java, ExchangeRequestService.java
    │   │
    │   ├── direct/                         # (기존 directRoom) 거래를 위한 유저 간 1:1 다이렉트 메시지 방
    │   │   ├── controller/DirectRoomController.java, DirectMessageController.java, UserConnectController.java, ExchangeChatController.java
    │   │   ├── dto/
    │   │   │   ├── request/ChatMessageRequest.java, CreateDirectRoomRequestDto.java, ReadMessageRequest.java
    │   │   │   └── response/ChatMessageResponse.java, DirectRoomCreateResponseDto.java, DirectRoomItemResponseDto.java, DirectRoomMessageResponse.java, DirectRoomResponseDto.java, RedisMessage.java
    │   │   ├── event/TradeAcceptedEventListener.java
    │   │   ├── model/DirectMessage.java, DirectRoom.java
    │   │   ├── pubsub/RedisDirectMessagePublisher.java, RedisDirectMessageSubscriber.java
    │   │   ├── repository/DirectMessageRepository.java, DirectRoomRepository.java
    │   │   └── service/DirectMessageService.java, DirectRoomService.java, UserConnectService.java, ExchangeChatService.java
    │   │
    │   └── ticketalarm/                    # (기존 ticketalarm) 오픈 일정 기반 예매 알림 예약
    │       ├── controller/TicketAlarmController.java
    │       ├── dto/
    │       │   ├── request/CreateTicketAlarmRequestDto.java, UpdateTicketAlarmRequestDto.java
    │       │   └── response/TicketAlarmResponseDto.java
    │       ├── model/ApplyScope.java, BaseType.java, TeamBookingPolicy.java, TicketAlarm.java
    │       ├── repository/TeamBookingPolicyRepository.java, TicketAlarmRepository.java
    │       └── service/TicketAlarmService.java
    │
    └── support/                            # 🛠️ [지원 인프라] 비즈니스를 보조하는 기술 및 외부 연동 인프라 컴포넌트
        ├── chat/                           # (기존 stompchat) 전사 공통 실시간 웹소켓 STOMP 메시징
        │   ├── controller/LiveBoardChatController.java, LocationController.java
        │   ├── dto/request/LocationRequestDto.java
        │   ├── dto/response/RedisUpdateDto.java
        │   ├── event/ItemLocationEventListener.java
        │   ├── eventlistener/WebSocketEventListener.java
        │   ├── interceptor/StompInterceptor.java, StompPrincipal.java
        │   ├── model/ChatDomainType.java, ChatMessage.java
        │   ├── pubsub/RedisChatPublisher.java, RedisChatSubscriber.java, RedisLocationPublisher.java, RedisLocationSubscriber.java, LiveBoardMatchSubscriber.java
        │   ├── registry/RedisUserSessionRegistry.java
        │   ├── repository/LiveBoardChatRepository.java
        │   └── service/ChatService.java, LocationService.java
        │
        ├── image/                          # (기존 image) AWS S3 및 로컬 파일 스토리지 추상화 계층
        │   ├── controller/ImageController.java
        │   └── service/ImageStorageService.java, LocalImageStorageServiceImpl.java
        │
        └── weather/                        # (기존 weather) 기상청 외부 API 연동 및 파싱 엔진
            ├── api/ApiTimeCalculator.java, WeatherApiUrlGenerator.java
            ├── controller/WeatherController.java
            ├── dto/ForeCastResponseDto.java, NowCastResponseDto.java, WeatherBundle.java
            ├── model/RainType.java, SkyStatus.java, Weather.java, WeatherApiStatus.java, WeatherStadium.java, WindDirection.java
            ├── response/OriginResponse.java
            ├── service/WeatherService.java
            └── util/LatLonToGrid.java, WeatherParser.java
```

## 📏 2. 우리가 반드시 지켜야 할 3가지 설계 룰 (Zero Magic)

구조만 바꾸는 것이 아니라, 패키지 간의 **의존성(Dependency) 방향**을 철저히 통제하여 유지보수성을 극대화합니다.

### 1. 하향식 단방향 의존성 유지 (방향성 통제)

의존성은 오직 하향식(Top-Down)으로만 흘러야 합니다.

- **허용 방향 (✅):**
  - `core` → `foundation`
  - `core` → `support`
  - `support` → `foundation`
- **🚫 절대 금지 (Zero Magic 위배):**
  - `foundation` → `core` (역방향 금지)
  - `foundation` → `support` (기반 도메인이 외부 인프라에 의존 금지)
  - `support` → `core` (역방향 금지)
  - `core` ↔ `core` (직접 참조 금지, 반드시 이벤트로 우회)

### 2. 핵심 도메인(`core`) 간의 직접 참조 금지 (느슨한 결합)

- `trade`(교환) 도메인과 `direct`(다이렉트 채팅) 도메인은 서로의 Service나 Repository를 `@Autowired`나 생성자로 직접 주입받아 호출하면 안 됩니다.
- **해결책 (Spring Event 활용):** 하나의 기능 완료 후 다른 도메인의 동작이 필요하다면 이벤트 기반(Event-Driven)으로 풀어냅니다.
  - *예: 교환(Trade)이 완료되었을 때 다이렉트 채팅방을 생성해야 한다면, `TradeService`에서 `ApplicationEventPublisher`로 `TradeAcceptedEvent`를 발행(Publish)하고, direct 도메인에서 이를 구독(Listen)하여 처리합니다.*

### 3. 통신망(Chat)과 비즈니스 로직의 분리

- 채팅 통신망(STOMP, Redis Pub/Sub)은 `support/chat` 패키지가 전담합니다. 이곳에는 비즈니스 로직이 없어야 합니다.
- 교환방(1:1)이나 라이브보드(단체방)에서 메시지를 받았을 때 상태를 바꾸거나 DB를 조작하는 등 **의미를 부여하는 비즈니스 로직**은 각각 `core/direct`나 상위 도메인에서 책임집니다.
- *이유: 나중에 채팅 서버 인스턴스만 별도로 분리(MSA)해야 할 때, 비즈니스 로직이 엉켜있으면 떼어낼 수 없기 때문입니다.*

### 🚨 프론트엔드 변경 필수 사항: 라이브보드 날씨 데이터 분리

- 기존에는 백엔드의 liveboard (라이브보드) 도메인 내부에서 기상청 데이터를 다루는 weather 도메인을 직접 끌어다 썼습니다.
- 하지만 이번에 **"핵심 비즈니스(Foundation/Core)는 외부 연동 인프라(Support)를 직접 참조해선 안 된다(Zero Magic)"**는 아키텍처 룰을 엄격하게 적용하면서 이 결합을 끊어냈습니다.
- 이로 인해 다음과 같은 변화가 발생합니다.

1. 복합 API의 삭제: 기존에 라이브보드 정보와 날씨 정보를 한 번에 묶어서 내려주던 API(예:  WeatherQueryController )가 삭제됩니다.
2. 응답 데이터(DTO) 축소: 라이브보드 방 입장 시 내려주던  LiveBoardRoomResponseDto  내부에서 날씨 관련 필드(예:  ForeCast ,  NowCast )가 제거됩니다.

#### 👉 프론트엔드(Expo) 팀의 대응 방안

앞으로는 프론트엔드 화면 단에서 두 개의 독립적인 API를 병렬(Parallel) 호출하여 직접 조합해야 합니다.

- GET /api/liveboard/room/{id}  (경기/채팅방 데이터 호출)
- GET /api/weather/{stadiumId}  (해당 구장의 기상청 날씨 호출)
- *※ Promise.all() 등을 활용해 두 API를 찌른 뒤, 프론트엔드 상태(State)에서 합쳐서 렌더링하도록 수정해야 합니다.*
