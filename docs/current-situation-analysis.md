# 백엔드 코드 상황 분석 보고서

## 개요

CodeRabbit PR 리뷰에서 지적된 5가지 항목에 대한 현재 코드 상황 분석. Gemini 3.1 Pro 모델이 설계 결정을 내릴 수 있도록 실제 코드 상태를 객관적으로 기록한다.

## 1. DirectRoom 부재 처리 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/exchangerequest/service/ExchangeRequestService.java`
**위치**: Lines 107-109

```java
directRoomRepository
    .findByExchangeRequestId(exchangeRequestId)
    .ifPresent(DirectRoom::complete);
```

### 분석

- 현재 `ifPresent()`를 사용하여 DirectRoom이 존재할 경우에만 `complete()`를 호출
- DirectRoom이 없는 경우 조용히 넘어감
- 같은 서비스의 Lines 81-83에서는 교환 수락 시 DirectRoom을 생성하고 있음

### 정합성 문제

- 교환 완료 단계에서 DirectRoom이 없는 것은 데이터 정합성 오류 가능성
- 채팅방 상태와 교환 상태가 불일치할 수 있음

## 2. 요청 DTO 상태 전이 계약 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/item/dto/request/UpdateItemStatusRequestDto.java`

```java
public record UpdateItemStatusRequestDto(
    @NotNull ItemStatus status
) {}
```

### 분석

- DTO가 도메인 `ItemStatus` enum을 직접 사용
- 현재 상태와 허용된 액션이 섞여 있음
- 서비스 로직에서 `REGISTERED` 상태는 거부되고, `FAILED` 요청은 `reopen()`으로 처리

### 상태 전이 맵

- `COMPLETED` → `item.complete()` 호출
- `FAILED` → `item.reopen()` 호출 (의미 불일치)
- `DELETED` → `item.deleteItem()` 호출
- `REGISTERED` → 서비스에서 거부

## 3. 재오픈된 아이템 createdDate 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/item/model/Item.java`

```java
public void complete() {
    this.status = ItemStatus.COMPLETED;
    this.createdDate = null;  // createdDate 초기화
}

public void deleteItem() {
    this.status = ItemStatus.DELETED;
    this.createdDate = null;  // createdDate 초기화
}

public void reopen() {
    this.status = ItemStatus.REGISTERED;
    // createdDate 복원 없음
}
```

### 분석

- `complete()`와 `deleteItem()`은 `createdDate`를 null로 설정
- `reopen()`은 상태만 `REGISTERED`로 변경하고 `createdDate`는 복원하지 않음
- 재오픈된 아이템이 `createdDate = null`인 상태로 활성화됨

### 영향

- `createdDate`에 의존하는 정렬/조회/유니크 규칙에 영향 가능성
- 활성 아이템이 null createdDate를 갖는 비정상 상태

## 4. 상태 변경 경로 조회 스코프 불일치

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`

**상태 변경 경로 (Lines 92-94)**:

```java
Item item = itemRepository.findById(itemId)
    .orElseThrow(() -> new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND));
```

**다른 경로들**:

```java
// deleteItem (Line 156)
Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);

// updateItem (Line 170)
Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);

// findItemById (Line 145)
Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
```

### 분석

- `updateItemStatus()`만 `findById()`를 사용하여 모든 아이템(과거/비활성 포함)에 접근 가능
- 다른 메서드들은 `findByIdAndStatusAndDateOrElseThrow()`로 오늘의 활성 아이템만 제한
- 조회 스코프 불일치로 의도하지 않은 상태 변경 가능성

## 5. 트랜잭션 커밋 전 WebSocket 알림 문제

### 현재 코드 상태

**파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`

**createItemWithImages (Line 82)**:

```java
@Transactional
public ItemResponseDto createItemWithImages(...) {
    // ... 아이템 생성 로직
    itemRepository.save(item);
    ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item, this);
    locationService.notifyUsersNearBy(user.getId(), "ADD_ITEM", newItemDto);  // 트랜잭션 내 알림
    return ItemResponseDto.from(item);
}
```

**updateItemStatus (Lines 99, 111)**:

```java
if (request.status() == ItemStatus.COMPLETED) {
    item.complete();
    Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
    locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);  // 트랜잭션 내 알림
    return;
}

if (request.status() == ItemStatus.DELETED) {
    item.deleteItem();
    Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
    locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);  // 트랜잭션 내 알림
    return;
}
```

**deleteItem (Line 161)**:

```java
@Transactional
public void deleteItem(TokenClaim tokenClaim, Long itemId) {
    // ... 삭제 로직
    item.deleteItem();
    Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
    locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);  // 트랜잭션 내 알림
}
```

**ExchangeRequestService.completeExchange (Line 112)**:

```java
@Transactional
public void completeExchange(Long exchangeRequestId) {
    // ... 완료 로직
    Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
    locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);  // 트랜잭션 내 알림
}
```

### 분석

- 모든 `@Transactional` 메서드 내에서 즉시 WebSocket 알림 전송
- `locationService.notifyUsersNearBy()`가 `messagingTemplate.convertAndSend()`를 직접 호출
- 트랜잭션 커밋 실패 시 클라이언트는 이미 변경 알림을 받았지만 실제 DB는 변경되지 않음

### 위험

- 클라이언트-서버 상태 불일치
- 데이터 정합성 문제

## 요약

5가지 항목 모두 CodeRabbit이 지적한 대로 현재 코드에 실제로 존재하는 문제들임. 각 항목은 데이터 정합성, 상태 관리, 트랜잭션 안정성 측면에서 개선이 필요한 상황임.

---

_작성일: 2026-03-11_  
_분석 대상: 백엔드 Spring Boot 코드_  
_목적: Gemini 3.1 Pro 설계 결정 지원_
