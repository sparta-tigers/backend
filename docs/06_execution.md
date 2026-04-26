# Task Prompt: 06. 코드 품질, 리팩토링 및 데이터 검증 (Refactoring & Validation)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **검증 로직의 파편화:** `ItemService`의 `createItemWithImages` 내부에서 활성 아이템 개수를 체크하는 로직이 컨트롤러(또는 헬퍼 메서드)와 중복으로 존재함. 비즈니스 룰이 분산되어 유지보수성이 저하됨.
- **OS 의존적인 비결정적 파일 메타데이터:** `OrphanImageCleanupScheduler`에서 파일의 `creationTime()`을 기준으로 오래된 파일을 식별하는데, 리눅스(Linux) 환경 등 일부 파일시스템에서는 생성 시간을 정확히 지원하지 않아 epoch(1970년)을 반환할 수 있음. 이로 인해 방금 생성된 정상 파일이 '오래된 고아 파일'로 오판되어 삭제될 치명적인 위험이 있음.
- **부동소수점 타입의 한계와 검증 누수:** 좌표나 정밀한 수치를 검증하는 DTO(`FindItemByIdRequestDto` 등)에서 `Double` 타입에 `@Min`/`@Max`를 적용하고 있음. Java Bean Validation 스펙상 부동소수점 타입은 오차로 인해 이를 공식 지원하지 않으므로, 미세한 오차 발생 시 검증을 통과해버리는 보안/정합성 누수가 발생함.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **단일 진실의 원천 (DRY & SSOT):** 비즈니스 검증 로직은 단 한 곳(헬퍼 메서드)에만 존재해야 한다. 중복 코드를 제거하고 응집도를 높인다.
- **결정론적 인프라 통제 (Deterministic Infrastructure):** OS 나 파일시스템의 암묵적 동작("알아서 생성 시간을 주겠지")에 기대지 않는다. 명시적으로 생성 시간과 수정 시간을 모두 읽어 가장 최근(Max) 시각을 안전하게 취하는 방어적 프로그래밍을 강제한다.
- **정적 타이핑 기반 정밀 검증 (Strict Type Validation):** 부동소수점을 기반으로 한 수치 제한은 "마법의 숫자"를 낳는다. 정밀한 값의 대소 비교가 필요하다면 오차가 없는 `BigDecimal`로 타입을 교체하고, 스펙에 맞는 애너테이션(`@DecimalMin`)을 사용한다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
- `src/main/java/com/sparta/spartatigers/domain/item/dto/request/FindItemByIdRequestDto.java` (또는 Double 좌표 검증이 있는 DTO)
- (통합 테스트 코드 추가) `src/test/java/com/sparta/spartatigers/domain/item/service/ItemServiceIntegrationTest.java` (경로에 맞게 조정)

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
  - **기존 함수/클래스** : `createItemWithImages`
  - **변경 사항** :
    - [ ] 내부에서 활성 아이템 개수를 DB에서 직접 세어 예외를 던지는 중복 코드를 완전히 삭제할 것.
    - [ ] 대신 외부에서 이미 분리해둔 `validateCanCreateItem` (또는 해당 헬퍼 메서드)를 호출하도록 로직을 일원화할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
  - **기존 함수/클래스** : `BasicFileAttributes` 읽는 부분
  - **변경 사항** :
    - [ ] `creationTime()` 에만 의존하는 코드를 버릴 것.
    - [ ] `Instant created = attrs.creationTime().toInstant();` 와 `Instant modified = attrs.lastModifiedTime().toInstant();` 를 모두 가져온 뒤, `created.isAfter(modified) ? created : modified` 삼항 연산자를 사용해 둘 중 더 최근 시간을 기준(`referenceTime`)으로 삼아 24시간 초과 여부를 계산할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/dto/request/FindItemByIdRequestDto.java` (및 관련 DTO)
  - **기존 함수/클래스** : `Double` 타입의 위경도/좌표 검증 필드
  - **변경 사항** :
    - [ ] 해당 필드의 타입을 `Double` 에서 `java.math.BigDecimal` 로 변경할 것.
    - [ ] `@Min` / `@Max` 애너테이션을 `@DecimalMin(value = "...", inclusive = true)` / `@DecimalMax(value = "...", inclusive = true)` 로 전부 교체할 것.

- **파일 경로** : `src/test/java/com/sparta/spartatigers/domain/item/...` (통합 테스트 파일)
  - **기존 함수/클래스** : `createItem` 관련 테스트
  - **변경 사항** :
    - [ ] 이미 활성 아이템이 있는 유저가 `createItem`을 호출했을 때 `ITEM_ALREADY_EXISTS` (409 에러 등) 예외가 정상적으로 매핑/발생하는지를 검증하는 통합 테스트 케이스를 1건 명시적으로 추가할 것.

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **운영체제 기능 맹신 금지:** Java의 NIO File API가 모든 환경에서 동일하게 동작할 것이라고 가정하지 마라.
- **Double/Float 타입으로 제약조건 검증 금지:** 소수점 오차가 발생할 수 있는 타입에 어노테이션을 붙여 Bean Validation이 알아서 해주기를 기대하지 마라.
- **Java 16 미만 문법 사용 지양:** `getRoomIdMap` 등 스트림 결과를 리스트로 모을 때 불필요하게 `Collectors.toList()`를 쓰지 말고, 불변 리스트인 `.toList()`를 사용하여 메모리와 가독성을 최적화하라.

## 6. 최종 검증 (Verification)

- 리눅스 서버에 배포했을 때 방금 생성된 이미지가 스케줄러에 의해 삭제되는 버그가 사라졌는가?
- DTO의 범위 한계치에 딱 맞는(예: 90.0) 값을 입력했을 때 부동소수점 오차로 인한 검증 실패 없이 깔끔하게 통과(또는 차단)되는가?
- 아이템 중복 생성 시도 시 409 Conflict 에러 코드가 정상적으로 반환되며 통합 테스트를 통과하는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
