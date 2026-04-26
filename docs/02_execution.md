# Task Prompt: 02. 성능 최적화, 메모리 관리 및 페이징 (Performance, Memory & Pagination)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **무제한 쿼리로 인한 메모리 폭주 (Unbounded Queries):** `findAllImageUrls`와 `findMessagesAfterTimestamp` 쿼리가 결과량(Limit) 제한 없이 전체 데이터를 메모리로 로드하고 있음. 이는 데이터가 커질수록 힙 메모리 고갈(OOM)과 GC 스파이크를 유발하는, 프레임워크의 편리함에 숨겨진 시한폭탄임.
- **루프 내 네트워크 호출 (N+1 I/O Problem):** `RedisUserSessionRegistry`의 `areUsersConnected` 메서드 내부에서 `for` 루프를 돌며 개별적으로 `redisTemplate.hasKey()`를 호출하고 있음. 인메모리 연산으로 착각하기 쉬우나, 실제로는 매번 Redis와의 네트워크 왕복이 발생하는 전형적인 N+1 병목임.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **결정론적 메모리 관리 (Deterministic Memory Limits):** 서버 메모리 사용량은 예측 가능해야 한다. 대량의 데이터를 조회할 때는 반드시 `Pageable`을 통한 청크 단위 조회나 `Stream`을 이용한 커서 기반 처리를 강제하여, 메모리 사용의 상한선(Boundary)을 명시적으로 통제한다.
- **단일 네트워크 트랜잭션 (Batching over Network):** 애플리케이션 레이어의 루프 안에서 DB나 캐시에 단건 조회를 던지는 "Zero Magic"에 위배되는 짓을 해선 안 된다. 여러 키의 상태를 확인해야 한다면 Redis Pipeline을 통해 네트워크 비용을 O(N)에서 O(1) 트랜잭션으로 압축한다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java`
- `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java` (호출부 트랜잭션 처리 포함)
- `src/main/java/com/sparta/spartatigers/domain/directRoom/repository/DirectMessageRepository.java`
- `src/main/java/com/sparta/spartatigers/domain/directRoom/registry/RedisUserSessionRegistry.java`

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java` & `OrphanImageCleanupScheduler.java`
  - **기존 함수/클래스** : `findAllImageUrls()`
  - **변경 사항** :
    - [ ] `List<String> findAllImageUrls()`를 `Stream<String> findAllImageUrls()`로 변경.
    - [ ] 스케줄러(`OrphanImageCleanupScheduler`)의 호출부 메서드에 `@Transactional(readOnly = true)`를 반드시 추가하여 Stream 영속성 컨텍스트를 유지할 것.
    - [ ] 스케줄러에서 Stream 사용 시 반드시 `try-with-resources` 블록을 사용하여 커서가 명시적으로 닫히도록 보장할 것.
  - **주의할 의존성** : `java.util.stream.Stream`

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/repository/DirectMessageRepository.java`
  - **기존 함수/클래스** : `findMessagesAfterTimestamp(...)`
  - **변경 사항** :
    - [ ] 무제한 조회를 막기 위해 파라미터에 `Pageable pageable`을 추가.
    - [ ] 또는 `@Query` 내부 구조를 유지해야 한다면 쿼리 최상단에 Spring Data의 `Limit` 구문을 적용하여 최대 반환 개수 상한을 강제할 것.
    - [ ] 파라미터에 인라인으로 작성된 `java.time.LocalDateTime`의 FQN(Fully Qualified Name)을 상단 `import java.time.LocalDateTime;`으로 정리할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/registry/RedisUserSessionRegistry.java`
  - **기존 함수/클래스** : `areUsersConnected(List<Long> userIds)`
  - **변경 사항** :
    - [ ] 내부의 `for` 루프를 통한 `isUserConnected` 개별 호출을 완전히 삭제할 것.
    - [ ] `redisTemplate.executePipelined(...)`를 도입하여, `userIds` 목록 전체에 대한 존재 여부를 한 번의 네트워크 I/O로 가져오도록 리팩토링할 것.
  - **주의할 의존성** : Redis Pipeline의 결과 리스트가 `userIds`의 순서와 정확히 매칭되도록 맵에 조합하는 로직을 견고하게 작성할 것.

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **컬렉션 통째로 메모리 적재 금지:** 대규모 데이터가 예상되는 곳에 `List<T>`를 무지성으로 반환하지 마라.
- **루프 내 네트워크 I/O 금지:** JPA의 N+1 쿼리든, Redis의 N+1 명령어든, 반복문 내부에서의 외부 리소스 호출은 절대 허용하지 않는다. 반드시 배치/파이프라인으로 묶어라.
- **스트림 자원 누수 금지:** JPA에서 `Stream<T>`를 반환받을 때 `try-with-resources`를 누락하여 데이터베이스 커서를 열어둔 채 방치하는 실수를 절대 금지한다.

## 6. 최종 검증 (Verification)

- 스케줄러가 백그라운드에서 동작할 때 `findAllImageUrls`가 메모리 스파이크 없이 정상적으로 동작하며 트랜잭션 에러를 뿜지 않는가?
- 사용자가 채팅방에 오랜만에 접속하여 `findMessagesAfterTimestamp`를 호출했을 때, 지정된 Limit/Page 크기만큼만 제한적으로 반환되는가?
- 여러 사용자의 온라인 상태를 조회할 때, Redis로 날아가는 명령어가 N개가 아닌 파이프라인 단일 호출로 처리되는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
