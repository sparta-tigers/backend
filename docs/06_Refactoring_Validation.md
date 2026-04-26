# PR Comments

## `createItemWithImages` 내부의 중복 활성 아이템 검사 — `validateCanCreateItem` 호출로 일원화 권장

### 문제 파일 및 라인 1

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java (4)`
- 라인: `78-82`

### 문제 1

- 컨트롤러에서 이미 `validateCanCreateItem(tokenClaim)` 을 호출한 뒤 이 메서드가 또 동일한 검사를 수행합니다.
- defense-in-depth 의 의미는 있지만 `validateCanCreateItem` 헬퍼를 직접 호출(또는 내부 검사 메서드를 추출)해서 한 곳에서만 정의되도록 통일하는 편이 유지보수성에 좋습니다.
- 또한 컨트롤러 호출 시점과 이 트랜잭션 시점 사이의 race 는 어차피 DB 의 `UK_ACTIVE_ITEM_PER_USER` 가 최종 방어선이 되므로 이 in-flight 체크는 빠른 실패 용도로만 유지하면 됩니다.

## `createItem` 리팩토링 LGTM

### 문제 파일 및 라인 2

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/controller/ItemController.java (1)`
- 라인: `67-114`

### 문제 2

- 이전 리뷰에서 지적된 (1) 수동 JSON 파싱으로 인한 Bean Validation 우회, (2) `System.out.println` 디버그 출력, (3) `deleteImages` 실패 시 원인 예외 가려짐 문제가 모두 해결되었습니다.
- 또한 `validateCanCreateItem` 을 이미지 업로드 전에 호출해 불필요한 디스크 I/O 를 줄인 흐름도 좋습니다.
- 한 가지 보강 의견:
  - `validateCanCreateItem` 의 사전 검사와 `createItemWithImages` 내부의 동일 검사 사이에 race condition window 가 존재합니다.
  - 실제 동시성 방어는 DB 레벨의 unique constraint(`UK_ACTIVE_ITEM_PER_USER`) 가 담당한다고 하셨는데, 그 unique 제약 위반 시 `GlobalExceptionHandler` 에서 `ITEM_ALREADY_EXISTS` 코드로 정확히 매핑되도록 통합 테스트 1건을 두는 것을 권장드립니다.

## `getRoomIdMap` 구현 깔끔합니다 — 다만 `Collectors.toList()` 대신 `Stream.toList()` 권장

### 문제 파일 및 라인 3

- 파일: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java (2)`
- 라인: `221-247`

### 문제 3

- 배치 조회로 N+1 을 피한 부분 좋습니다.
- Java 16+ 환경이라면 `requests.stream().map(ExchangeRequest::getId).toList()` 로 한 줄 줄일 수 있습니다(추가로 불변 리스트가 반환되므로 의도가 명확).
- 또한 L237 의 `HashMap` 초기 용량 힌트(`new HashMap(requestIds.size() * 2)`)를 주면 minor allocation 최적화도 가능합니다.

## `creationTime()`은 리눅스 파일시스템에서 신뢰성이 낮음

### 문제 파일 및 라인 4

- 파일: `src/main/java/com/sparta/spartatigers/domain/image/scheduler/OrphanImageCleanupScheduler.java`
- 라인: `87-96`

### 문제 4

- `BasicFileAttributes.creationTime()`은 파일시스템(특히 ext4 일부 환경)이나 마운트 옵션에 따라 `lastModifiedTime`과 동일하거나 epoch(1970-01-01)을 반환할 수 있어, 실제로는 24시간이 지나지 않은 신규 파일을 "오래된 파일"로 오판해 삭제할 가능성이 있습니다.
- 안전을 위해 `creationTime`과 `lastModifiedTime` 중 더 최근 시각을 기준으로 비교하거나, 운영 파일시스템에서 creation time이 정확히 기록되는지 사전에 검증하는 편이 안전합니다.

### 제안 수정 4

```suggestion
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    Instant created = attrs.creationTime().toInstant();
                    Instant modified = attrs.lastModifiedTime().toInstant();
                    Instant referenceTime = created.isAfter(modified) ? created : modified;
                    if (referenceTime.isAfter(threshold)) {
                        log.debug("[OrphanImageCleanup] 최근 파일이므로 건너뜀: {}", fileName);
                        continue;
                    }
                } catch (IOException e) {
                    log.warn("[OrphanImageCleanup] 파일 속성 읽기 실패: {}", fileName, e);
                    continue;
                }
```

## `Double` 필드는 검증 애너테이션을 사용하기 전에 타입 재검토가 필요합니다

### 문제 파일 및 라인 5

- 파일: `src/main/java/com/sparta/spartatigers/domain/item/dto/request/FindItemByIdRequestDto.java`
- 라인: `8-14`

### 문제 5

- Jakarta Bean Validation 공식 스펙상 `@Min`/`@Max`와 `@DecimalMin`/`@DecimalMax` 모두 `Double`/`Float` 같은 부동소수점 타입을 공식 지원하지 않습니다.
- 반올림 오차로 인한 부정확성 때문입니다.
- 현재 구현은 좌표값 검증을 위해 `Double`을 사용 중이지만, 정밀한 수치 검증이 필요한 경우 `BigDecimal`로 변경하고 `@DecimalMin`/`@DecimalMax`를 사용하는 것을 권장합니다.
- 또는 검증 로직을 리뷰하여 필요성을 재평가하시기 바랍니다.
