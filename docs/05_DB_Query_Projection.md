# PR Comments

## Haversine 식의 `acos` 부동소수점 오버플로 가능성

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/repository/ItemRepository.java`
- 라인: `28-51`

### 문제 1

- `acos(...)` 인자는 이론상 [-1, 1] 범위지만, 두 좌표가 매우 가깝거나 동일한 경우 부동소수점 오차로 `1.0`을 살짝 초과한 값이 들어가 `NaN`을 반환할 수 있고, 이는 `=` 비교에서 항상 false가 되어 실제로 가까운 아이템이 결과에서 누락되는 결과를 초래합니다.
- 표준 방어책으로 `acos(LEAST(1.0, ...))`(또는 DB에 따라 `LEAST(1, expr)`)을 적용하는 것을 권장합니다.
- 또한 인덱스 활용을 위해 Haversine을 적용하기 전 위경도 박스 prefilter(`i.latitude BETWEEN ... AND ... AND i.longitude BETWEEN ... AND ...`)를 적용하면 풀스캔을 피할 수 있습니다.
- main 쿼리와 countQuery에 동일한 술어가 중복되는 점은 JPA 제약상 어쩔 수 없으나, prefilter를 추가하면 두 쿼리 모두 인덱스 활용도가 크게 개선됩니다.

## `Object[]` 원시 투영 — 가독성/유지보수성 개선 권장

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/domain/directRoom/service/DirectRoomService.java (3)`
- 라인: `89-94`

### 문제 2

- `countUnreadMsgInBatch` 가 `ListObject[]` 를 반환하는 부분은 인덱스 접근(`row[0]`, `row[1]`)이 컴파일러 검증을 우회해 추후 쿼리 변경 시 ClassCastException 으로 이어지기 쉽습니다.
- `interface UnreadCountProjection { Long getRoomId(); Long getCount(); }` 같은 Spring Data Projection 으로 바꾸는 편을 권장드립니다.
