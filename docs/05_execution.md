# Task Prompt: 05. 데이터베이스 쿼리 및 프로젝션 (DB Query & Projection)

## 1. 근본 원인 분석 (Root Cause Analysis)

- **부동소수점 오버플로에 의한 NaN 발생 (Floating-Point Precision Issue):** `ItemRepository`의 Haversine 거리 계산 식에서 두 좌표가 극도로 가깝거나 같을 경우, 부동소수점 정밀도 오차로 인해 `acos()`의 인자가 1.0을 아주 미세하게 초과하는 일이 발생함. 이 경우 DB는 `NaN`을 반환하며, 결과적으로 필터링 로직에서 해당 데이터가 완전히 누락되는 치명적인 데이터 정합성 오류가 발생함. 또한, B-Tree 인덱스를 탈 수 없는 복잡한 연산을 전체 테이블에 수행하여 풀스캔 병목을 유발하고 있음.
- **타입 안정성이 결여된 매직 인덱스 (Magic Index Projection):** `DirectRoomService` 등에서 조인 쿼리의 결과를 원시 타입인 `Object[]`로 반환받아 `row[0]`, `row[1]` 식으로 접근하고 있음. 이는 컬럼 순서가 바뀌거나 타입이 변경될 때 컴파일러가 잡아주지 못하며 런타임 예외를 일으키는, 프레임워크의 나쁜 마법이자 기술 부채임.

## 2. 작업 목표 및 카파시 설계 의도 (Objective & Architectural Intent)

- **결정론적 수학 연산 (Deterministic Math Bounds):** 수식의 입력값은 어떠한 경우에도 수학적 도메인(Domain)을 벗어나지 않도록 DB 레벨에서 하드 리미트(`LEAST`)를 걸어 방어한다.
- **공간 쿼리 최적화 (Pre-filtering over Full Scan):** 무거운 삼각함수 연산을 수행하기 전에, 인덱스를 탈 수 있는 단순 범위 조건(Bounding Box)을 먼저 걸어 연산 모수를 극단적으로 줄인다.
- **명시적 정적 타이핑 (Explicit Static Typing):** 쿼리의 결과는 반드시 DTO나 인터페이스로 바인딩(Projection)되어야 한다. 컴파일 타임에 타입과 필드명이 강제되지 않는 `Object[]` 사용을 시스템에서 완전히 축출한다.

## 3. 수정 대상 파일 (Target Files)

- `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java`
- `src/main/java/com/sparta/spartatigers/domain/directRoom/repository/DirectRoomRepository.java` (Object[] 반환 쿼리가 있는 레포지토리)
- `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java` (Object[] 매핑을 소비하는 서비스)

## 4. 파일별 상세 수정 지시 (Implementation Details)

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java`
  - **기존 함수/클래스** : Haversine 식을 사용하는 `@Query` 메서드
  - **변경 사항** :
    - [ ] `acos(...)` 내부의 계산식을 `acos(LEAST(1.0, ...))` 으로 감싸서 1.0을 초과하는 값이 절대 들어가지 않도록 방어할 것.
    - [ ] WHERE 절 최상단에 위경도 Bounding Box Prefilter를 명시적으로 추가할 것. (예: `i.latitude BETWEEN :minLat AND :maxLat AND i.longitude BETWEEN :minLon AND :maxLon`).
    - [ ] 서비스 레이어에서 위 Bounding Box의 최대/최소 위경도 파라미터를 계산하여 넘기도록 함께 수정할 것.

- **파일 경로** : `src/main/java/com/sparta/spartatigers/domain/directRoom/repository/DirectRoomRepository.java` & `DirectRoomService.java`
  - **기존 함수/클래스** : `Object[]`를 반환하는 커스텀 쿼리 및 이를 매핑하는 로직
  - **변경 사항** :
    - [ ] 레포지토리의 반환 타입을 `Object[]`에서 명시적인 Spring Data Projection 인터페이스(예: `RoomItemProjection`) 또는 DTO 생성자 표현식(`SELECT new com.sparta...MyDto(...)`)으로 리팩토링할 것.
    - [ ] 서비스 레이어에서 `Object[] row`를 받아 `(Long) row[0]`, `(String) row[1]` 식으로 형변환(Casting)하는 불안전한 로직을 전부 삭제하고, 강타입(Strong Type)의 게터(Getter)를 사용하도록 변경할 것.

## 5. 절대 하지 말아야 할 것 (Constraints & DO NOTs)

- **원시 배열 프로젝션 절대 금지:** JPA 쿼리에서 `Object[]`나 `List<Object[]>`를 반환하여 서비스 레이어에서 인덱스로 파싱하는 짓을 허용하지 마라. 모든 것은 컴파일러가 검증할 수 있는 DTO 혹은 인터페이스로 구성하라.
- **전체 테이블 삼각함수 연산 금지:** 좌표 기반 거리 계산 시, 사각 박스 범위(Bounding Box)를 통한 1차 필터링 없이 테이블 전체에 대해 거리 계산 함수를 돌리지 마라.
- **DB 엔진에 에러 핸들링 위임 금지:** 부동소수점 에러를 DB가 적당히 무시해주길 기대하지 마라. `LEAST`, `GREATEST` 함수를 통해 애플리케이션 프로그래머가 직접 값의 한계를 통제해야 한다.

## 6. 최종 검증 (Verification)

- 사용자의 현재 위치와 동일한 위치에 있는 아이템을 검색했을 때, 오버플로로 인한 누락 없이 0km (또는 근사치) 거리로 정상 반환되는가?
- `Object[]` 배열의 인덱스 접근 로직이 소스코드에서 완벽히 제거되었으며, DTO/인터페이스로 깔끔하게 매핑되는가?

🚨 **시스템 지시사항: 모든 수정 코드는 `// ...기존 로직...` 같은 생략 없이, 함수 단위 전체를 온전하게 작성하라.**
