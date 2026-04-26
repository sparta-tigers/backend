# Task Prompt: 03. 트랜잭션, 동시성 제어 및 스케줄링 (Transaction, Concurrency & Scheduling)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **영속성 컨텍스트 범위를 벗어난 지연 로딩 (Detached Entity Lazy Loading):** `ItemService`의 `afterCommit` 훅 내부에서 `sender`/`exchangeRequest` 엔티티의 필드를 참조하고 있음. 트랜잭션이 커밋되어 영속성 컨텍스트가 종료(Detached 상태)된 이후에 프록시 객체를 초기화하려고 시도하므로 `LazyInitializationException`이 발생할 수밖에 없는, 프레임워크 생명주기에 대한 잘못된 의존성 문제.
- **트랜잭션 경계 누락 (Missing Transaction Boundary):** `getRoomsForUser` 등 복합적인 지연 로딩과 여러 레포지토리 호출이 일어나는 읽기 로직에 `@Transactional(readOnly = true)`가 누락되어, 일관된 스냅샷(Snapshot)을 보장받지 못하고 커넥션 자원을 비효율적으로 낭비함.
- **비결정적 스케줄링 및 Race Condition 우려:** `OrphanImageCleanupScheduler`의 cron 식이 JVM 타임존에 의존하고 있어 배포 환경(UTC vs KST)에 따라 실행 시간이 틀어짐. 또한 `rejectOtherPendingRequests` 로직은 다중 접속 시 동시에 수락/요청이 들어올 경우 상태가 꼬일 수 있는 경합 조건(Race Condition)에 무방비 상태임.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **Value-Only Event Payload (Zero Magic):** 비동기/이벤트 훅(`afterCommit` 등)으로 데이터를 넘길 때는 "마법의 영속성 객체(Entity)"를 절대 넘기지 않는다. 트랜잭션 내부에서 필요한 순수 값(Primitive Types, String 등)을 미리 추출하여 결정론적(Deterministic)인 상태로만 전달한다.
- **명시적 락킹 (Explicit Locking):** 프레임워크가 알아서 동시성을 제어해주지 않는다. 동시 변경 위험이 있는 비즈니스 로직은 DB 레벨의 명시적 락(Pessimistic Lock)을 통해 동시성 문제를 원천 차단한다.
- **명시적 환경 통제 (Deterministic Environment):** 스케줄러 타임존 등 런타임 환경에 의존하는 설정을 제거하고, 코드 레벨에서 명시적으로 시간대를 고정한다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java` (또는 getRoomsForUser가 위치한 클래스)
- `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
- `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java`
- `src/main/java/com/sparta/spartatigers/domain/exchangerequest/repository/ExchangeRequestRepository.java`

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
  - **기존 함수/클래스** : `afterCommit` 람다를 사용하는 로직
  - **변경 사항** :
    - [ ] `TransactionSynchronizationManager.registerSynchronization` 내부의 `afterCommit` 람다로 `sender`나 `exchangeRequest` 같은 영속성 엔티티를 통째로 넘기지 말 것.
    - [ ] 람다 실행 전(트랜잭션 내부)에 `Long senderId = sender.getId();`, `String token = sender.getDeviceToken();` 등 필요한 원시값(Primitive/String)들을 변수로 선언하고, 람다 내부에서는 이 변수들만 캡처(Capture)하여 사용할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java`
  - **기존 함수/클래스** : `getRoomsForUser(Long currentUserId, Pageable pageable)`
  - **변경 사항** :
    - [ ] 메서드 상단에 `@Transactional(readOnly = true)` 애너테이션을 명시적으로 부착하여 단일 트랜잭션 컨텍스트로 묶을 것.
    - [ ] `opponentIds`를 추출하는 스트림 로직에 `.distinct()`를 추가하여 중복된 상대방 ID로 인한 불필요한 캐시/DB 추가 조회를 방지할 것. (`.distinct().toList()` 적용)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
  - **기존 함수/클래스** : `@Scheduled` 애너테이션
  - **변경 사항** :
    - [ ] `@Scheduled(cron = "0 0 3 * * *")`에 `zone = "Asia/Seoul"` 속성을 명시적으로 추가하여 배포 서버의 시스템 타임존(OS 설정)을 타는 불확실성을 제거할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java` & `Repository`
  - **기존 함수/클래스** : `rejectOtherPendingRequests(...)` 및 관련 레포지토리 메서드
  - **변경 사항** :
    - [ ] 동시 교환 수락을 막기 위해 레포지토리에 조회용 커스텀 메서드(예: `findPendingRequestsByItemIdForUpdate`)를 만들고 `@Lock(LockModeType.PESSIMISTIC_WRITE)`를 적용할 것.
    - [ ] `rejectOtherPendingRequests` 내부에서 위 락이 걸린 쿼리를 사용하여 데이터를 가져오도록 리팩토링할 것.

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **Detached 객체 지연 로딩 절대 금지:** 트랜잭션 범위 밖(`afterCommit`, 별도 스레드 등)에서 Hibernate 프록시 객체의 필드를 `.get()`으로 찔러보는 마법에 의존하지 마라. 모든 데이터는 트랜잭션 내에서 DTO나 스칼라(Scalar) 값으로 변환되어 전달되어야 한다.
- **OSIV 암묵적 의존 금지:** Open Session In View에 의존하여 서비스 레이어 외부에서 지연 로딩이 동작할 것이라 기대하지 마라. 읽기 전용 메서드는 반드시 `@Transactional(readOnly = true)`로 명시적 경계를 설정하라.
- **애플리케이션 레이어 동시성 방치 금지:** 여러 유저가 얽힌 상태 변경(`REJECT`, `ACCEPT`)을 데이터베이스 레벨의 락(Lock) 없이 순수 자바 로직으로만 처리하지 마라.

## 6. 최종 검증 (Verification)

- 알림 발송 로직(`afterCommit` 내부)에서 `LazyInitializationException`이 발생하지 않는가?
- `getRoomsForUser` 호출 시 영속성 컨텍스트가 유지되며 동일한 `opponentId`가 중복해서 조회되지 않는가?
- 교환 수락 처리가 동시에 여러 요청으로 들어올 때 DB 비관적 락에 의해 Race Condition이 방어되는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
