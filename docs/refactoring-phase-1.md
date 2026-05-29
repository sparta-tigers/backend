# Phase 1: Foundation 계층 구축

> **목표:** 시스템의 뿌리가 되는 기반 도메인(User, Baseball)을 `domain/foundation/` 하위로 이동한다.
> 이 Phase가 가장 크고 복잡하다 — 특히 `liveboard` 36개 파일의 분해가 핵심이다.

---

## 0. 시작 전 사전 확인 (Synchronization)

이 Phase는 파일 이동 범위가 가장 넓으므로, 반드시 의존성 그래프 분석 단계를 거친다.

- **의존성 그래프 분석:** `AuthArgumentResolver`와 `TokenClaim` 이동이 호출부에 미칠 영향을 위해, `TokenClaim`을 참조하는 모든 Controller의 리스트를 미리 뽑아두고 시작한다.

## 1-1. common (변경 없음)

`domain/common/entity/BaseEntity.java`는 현재 위치를 유지한다.

---

## 1-2. TokenClaim 분리 (global/aop로 이동)

> **근거:** TokenClaim은 12개 도메인의 모든 Controller에서 사용되는 교차 관심사. `AuthArgumentResolver`와 같은 패키지에 위치해야 한다.

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/auth/model/TokenClaim.java` | `global/aop/TokenClaim.java` |

**import 치환:**
```
com.sparta.spartatigers.domain.auth.model.TokenClaim
→ com.sparta.spartatigers.global.aop.TokenClaim
```

> ⚠️ **이 치환은 거의 모든 Controller 파일에 영향을 준다.** IDE의 Find & Replace in Files로 일괄 처리한다.

---

## 1-3. User 도메인 그룹 → `domain/foundation/user/`

### 1-3-1. auth → `foundation/user/auth/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/auth/client/KakaoClient.java` | `domain/foundation/user/auth/client/` |
| `domain/auth/controller/AuthController.java` | `domain/foundation/user/auth/controller/` |
| `domain/auth/dto/` (7 files) | `domain/foundation/user/auth/dto/` |
| `domain/auth/model/OAuthProvider.java` | `domain/foundation/user/auth/model/` |
| `domain/auth/model/Oauth.java` | `domain/foundation/user/auth/model/` |
| `domain/auth/model/RefreshToken.java` | `domain/foundation/user/auth/model/` |
| `domain/auth/model/Token.java` | `domain/foundation/user/auth/model/` |
| `domain/auth/repository/` (2 files) | `domain/foundation/user/auth/repository/` |
| `domain/auth/service/` (5 files) | `domain/foundation/user/auth/service/` |

> **주의:** `TokenClaim.java`는 1-2에서 이미 `global/aop/`로 이동 완료.

**import 치환:**
```
com.sparta.spartatigers.domain.auth.
→ com.sparta.spartatigers.domain.foundation.user.auth.
```

### 1-3-2. user → `foundation/user/account/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/user/controller/UserRestController.java` | `domain/foundation/user/account/controller/` |
| `domain/user/dto/` (2 files) | `domain/foundation/user/account/dto/` |
| `domain/user/model/LoginUser.java, User.java, UserRole.java` | `domain/foundation/user/account/model/` |
| `domain/user/repository/UserRepository.java` | `domain/foundation/user/account/repository/` |
| `domain/user/service/UserService.java` | `domain/foundation/user/account/service/` |

**import 치환:**
```
com.sparta.spartatigers.domain.user.
→ com.sparta.spartatigers.domain.foundation.user.account.
```

### 1-3-3. favoriteteam → `foundation/user/favoriteteam/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/favoriteteam/` (전체 6 files) | `domain/foundation/user/favoriteteam/` |

**import 치환:**
```
com.sparta.spartatigers.domain.favoriteteam.
→ com.sparta.spartatigers.domain.foundation.user.favoriteteam.
```

---

## 1-4. Baseball 도메인 그룹 → `domain/foundation/baseball/`

### 1-4-1. team → `foundation/baseball/team/`

| 현재 경로 | 이동 경로 | 비고 |
|---|---|---|
| `domain/team/model/Team.java` | `domain/foundation/baseball/team/model/` | |
| `domain/team/model/TeamCode.java` | `domain/foundation/baseball/team/model/` | SSOT 메타데이터 |
| `domain/team/model/Player.java` | `domain/foundation/baseball/team/model/` | |
| `domain/team/repository/TeamRepository.java` | `domain/foundation/baseball/team/repository/` | |
| `domain/liveboard/model/Stadium.java` | `domain/foundation/baseball/team/model/` | liveboard에서 이동 |
| `domain/liveboard/repository/StadiumRepository.java` | `domain/foundation/baseball/team/repository/` | liveboard에서 이동 |

> ⚠️ **확인 필요:** `domain/liveboard/model/Player.java`와 `domain/team/model/Player.java`가 동일 클래스인지, 별개 클래스인지 코드 수준에서 확인 후 통합 또는 리네이밍 결정.

**import 치환:**
```
com.sparta.spartatigers.domain.team.
→ com.sparta.spartatigers.domain.foundation.baseball.team.
```

### 1-4-1-1. [위험 요소] Player 모델 통합 전략

`liveboard/model/Player.java`와 `team/model/Player.java`가 존재함.

- **분석:** 두 클래스의 필드를 비교하여, `liveboard`의 선수 데이터가 `team` 도메인의 데이터를 포함하거나 더 상세한지 확인한다.
- **결정:** - 두 모델을 **`foundation/baseball/team/model/Player.java`로 통합**한다.
  - 단, 라이브보드에서만 필요한 임시 필드(예: 타석 기록 등)가 있다면, 이는 `Player` 모델을 직접 수정하지 말고 `Match`와 연관된 별도의 `MatchPlayerStats` 등으로 분리하여 결합도를 낮춘다.

### 1-4-2. liveboard 분해 → `foundation/baseball/match/`

**이것이 Phase 1의 핵심이다.** 36개 파일을 4개 대상으로 분산한다.

#### Match 도메인 (foundation/baseball/match/)

| 현재 경로 | 이동 경로 | 역할 |
|---|---|---|
| **Controller** | | |
| `liveboard/controller/MatchController.java` | `foundation/baseball/match/controller/` | 경기 일정 조회 |
| `liveboard/controller/LiveBoardRoomController.java` | `foundation/baseball/match/controller/` | 라이브보드 룸 CRUD |
| **DTO** | | |
| `liveboard/dto/MatchScheduleResponseDto.java` | `foundation/baseball/match/dto/` | |
| `liveboard/dto/LiveBoardDataResponseDto.java` | `foundation/baseball/match/dto/` | |
| `liveboard/dto/LiveBoardRoomResponseDto.java` | `foundation/baseball/match/dto/` | |
| **Model** | | |
| `liveboard/model/Match.java` | `foundation/baseball/match/model/` | 핵심 엔티티 |
| `liveboard/model/MatchResult.java` | `foundation/baseball/match/model/` | |
| `liveboard/model/MatchScore.java` | `foundation/baseball/match/model/` | |
| `liveboard/model/InningTexts.java` | `foundation/baseball/match/model/` | |
| `liveboard/model/HomeAway.java` | `foundation/baseball/match/model/` | Enum |
| `liveboard/model/LeagueType.java` | `foundation/baseball/match/model/` | Enum |
| `liveboard/model/LiveBoardStatus.java` | `foundation/baseball/match/model/` | Enum |
| `liveboard/model/LiveBoardConnection.java` | `foundation/baseball/match/model/` | 실시간 연결 |
| `liveboard/model/LiveBoardData.java` | `foundation/baseball/match/model/` | 실시간 데이터 |
| `liveboard/model/LiveBoardRoom.java` | `foundation/baseball/match/model/` | 라이브 룸 |
| **Repository** | | |
| `liveboard/repository/MatchRepository.java` | `foundation/baseball/match/repository/` | |
| `liveboard/repository/LiveBoardConnectionRepository.java` | `foundation/baseball/match/repository/` | |
| `liveboard/repository/LiveBoardRoomRepository.java` | `foundation/baseball/match/repository/` | |
| **Service** | | |
| `liveboard/service/MatchScheduleService.java` | `foundation/baseball/match/service/` | |
| `liveboard/service/LiveBoardDataService.java` | `foundation/baseball/match/service/` | |
| `liveboard/service/LiveBoardMatchService.java` | `foundation/baseball/match/service/` | |
| `liveboard/service/LiveboardRoomService.java` | `foundation/baseball/match/service/` | |
| **Util** | | |
| `liveboard/util/GlobalSessionGenerator.java` | `foundation/baseball/match/util/` | |

#### Lineup 도메인 (foundation/baseball/lineup/)

| 현재 경로 | 이동 경로 |
|---|---|
| `liveboard/controller/LineupController.java` | `foundation/baseball/lineup/controller/` |
| `liveboard/dto/LineupBatterResponse.java` | `foundation/baseball/lineup/dto/` |
| `liveboard/dto/LineupCacheDto.java` | `foundation/baseball/lineup/dto/` |
| `liveboard/dto/LineupResponseDto.java` | `foundation/baseball/lineup/dto/` |
| `liveboard/model/LineupBatter.java` | `foundation/baseball/lineup/model/` |

기존 `domain/startinglineup/`도 여기에 합류:

| 현재 경로 | 이동 경로 |
|---|---|
| `startinglineup/model/LineupPlayer.java` | `foundation/baseball/lineup/model/` |
| `startinglineup/model/Position.java` | `foundation/baseball/lineup/model/` |
| `startinglineup/model/StartingLineup.java` | `foundation/baseball/lineup/model/` |
| `startinglineup/model/StartingLineupPK.java` | `foundation/baseball/lineup/model/` |
| `startinglineup/repository/StartingLineupRepository.java` | `foundation/baseball/lineup/repository/` |
| `startinglineup/service/StartingLineupService.java` | `foundation/baseball/lineup/service/` |
| `liveboard/service/LineupQueryService.java` | `foundation/baseball/lineup/service/` |

#### Redis 인프라 → support (Phase 2에서 처리)

| 현재 경로 | 이동 경로 | Phase |
|---|---|---|
| `liveboard/pubsub/LiveBoardMatchSubscriber.java` | `support/chat/pubsub/` | Phase 2 |

> DIP 적용: `LiveBoardMatchService`가 구독 데이터를 받는 인터페이스를 foundation에 정의하고, support의 subscriber가 이를 구현한다.

#### Weather 결합 해소 (foundation → support 금지)

| 현재 경로 | 처리 방법 |
|---|---|
| `controller/WeatherQueryController.java` | 삭제 (완전 제거) |
| `service/WeatherQueryService.java` | 삭제 (완전 제거) |
| `dto/MatchWeatherResponse.java` | 삭제 (완전 제거) |
| `dto/LiveBoardRoomResponseDto.java` | 날씨 관련 필드(ForeCast 등) 및 import 제거 |
| `service/LiveboardRoomService.java` | `WeatherService` 호출 로직 및 import 제거 |

**권장 처리:** 프론트엔드에서 경기 API(`/api/match/{id}`)와 날씨 API(`/api/weather/{stadiumId}`)를 병렬 호출하도록 변경. 백엔드에서 합칠 필요가 있다면 `core/` 계층에 `MatchWeatherFacade`를 두어 foundation(match)과 support(weather)를 조립한다.

**import 치환:**
```
com.sparta.spartatigers.domain.liveboard.
→ com.sparta.spartatigers.domain.foundation.baseball.match.  (match 관련)
→ com.sparta.spartatigers.domain.foundation.baseball.lineup.  (lineup 관련)
→ com.sparta.spartatigers.domain.foundation.baseball.team.    (Stadium, Player)
```

### 1-4-3. ranking → `foundation/baseball/ranking/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/ranking/` (전체 9 files) | `domain/foundation/baseball/ranking/` |

> ranking은 `team(TeamCode, QTeam)`과 `liveboard(MatchRepository)` QueryDSL을 참조한다.
> Phase 1 후에는 모두 `foundation/baseball/` 내부이므로 동일 계층 참조 — 의존성 위반 없음.

**import 치환:**
```
com.sparta.spartatigers.domain.ranking.
→ com.sparta.spartatigers.domain.foundation.baseball.ranking.
```

### 1-4-4. home + dashboard → `foundation/baseball/home/`

| 현재 경로 | 이동 경로 |
|---|---|
| `domain/home/dto/HomeResponseDto.java` | `domain/foundation/baseball/home/dto/` |
| `domain/home/service/HomeService.java` | `domain/foundation/baseball/home/service/` |
| `domain/home/service/HomeMatchScheduleService.java` | `domain/foundation/baseball/home/service/` |
| `domain/home/service/HomeRankingService.java` | `domain/foundation/baseball/home/service/` |
| `domain/home/service/HomeTodayGameService.java` | `domain/foundation/baseball/home/service/` |
| `domain/dashboard/controller/DashboardController.java` | `domain/foundation/baseball/home/controller/` |
| `domain/dashboard/dto/HomeDashboardResponseDto.java` | `domain/foundation/baseball/home/dto/` |
| `domain/dashboard/service/DashboardService.java` | `domain/foundation/baseball/home/service/` |

> home/dashboard는 user, match, favoriteteam을 참조하는 read-model aggregator.
> Phase 1 후에는 모두 foundation 내부이므로 의존성 위반 없음.

---

## 1-5. Phase 1 의존성 정리 요약

| 이동 전 import 패턴 | 이동 후 import 패턴 |
|---|---|
| `domain.auth.model.TokenClaim` | `global.aop.TokenClaim` |
| `domain.auth.*` | `domain.foundation.user.auth.*` |
| `domain.user.*` | `domain.foundation.user.account.*` |
| `domain.favoriteteam.*` | `domain.foundation.user.favoriteteam.*` |
| `domain.team.*` | `domain.foundation.baseball.team.*` |
| `domain.liveboard.model.Match` | `domain.foundation.baseball.match.model.Match` |
| `domain.liveboard.model.Stadium` | `domain.foundation.baseball.team.model.Stadium` |
| `domain.liveboard.dto.Lineup*` | `domain.foundation.baseball.lineup.dto.*` |
| `domain.startinglineup.*` | `domain.foundation.baseball.lineup.*` |
| `domain.ranking.*` | `domain.foundation.baseball.ranking.*` |
| `domain.home.*` | `domain.foundation.baseball.home.*` |
| `domain.dashboard.*` | `domain.foundation.baseball.home.*` |

---

## 1-6. Phase 1 검증 체크리스트

- [ ] `scripts/verify.sh` 실행 → 컴파일 성공
- [ ] 전체 테스트 Green
- [ ] 원본 디렉토리 삭제 확인 (`domain/startinglineup/`, `domain/ranking/`, `domain/dashboard/`, `domain/favoriteteam/`, `domain/auth/`, `domain/user/`, `domain/home/`, `domain/team/`)
- [ ] `domain/liveboard/` 디렉토리는 `LiveBoardMatchSubscriber.java` (Phase 2 이동 대기)가 남아있으므로 **삭제하지 않고 유지**
- [ ] IDE의 'Optimize Imports' 등을 활용하여, 전체 프로젝트 내 구버전 import가 Phase 1 신규 경로로 완벽히 치환되었는지 검증
- [ ] QueryDSL Q-class 재생성 확인 (`./gradlew clean compileJava`)
- [ ] 기존 `liveboard/model/Player.java`와 `team/model/Player.java` 통합 여부 결정 완료
- [ ] WeatherQuery* 파일 삭제 완료 확인 (WeatherQueryController, WeatherQueryService, MatchWeatherResponse)
- [ ] `git tag phase-1-done`
