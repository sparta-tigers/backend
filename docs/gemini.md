# [에이전트 작업 지시서] 백엔드 아키텍처 및 품질 결함 4건 최종 청산 (Phase 12 ~ 15)

## 📌 목적

CodeRabbit PR 리뷰를 통해 식별된 스프링 컨텍스트(Spring Context) 활용 미흡, HTTP 표준 위반, 데이터 파싱 오류 등 4가지 결함을 수정한다. 이 작업은 애플리케이션의 성능, 일관성, 그리고 글로벌 웹 표준(RFC) 준수를 보장하기 위함이다.

---

## ⚡ Phase 12: 비동기(Async) 처리 활성화 및 이벤트 파이프라인 정립 (Issue 1)

현재 `ItemLocationEventListener`에 `@Async` 어노테이션을 붙여놓았으나, 정작 스프링 부트 설정에 `@EnableAsync`가 누락되어 모든 이벤트가 동기(Synchronous)로 처리되고 있다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/config/AsyncConfig.java` (신규 생성)
- **지시사항**:
  - `config` 패키지 하위에 `AsyncConfig` 클래스를 신규 생성하라.
  - 해당 클래스에 `@Configuration`과 `@EnableAsync` 어노테이션을 부착하여 애플리케이션 전역에서 비동기 처리가 가능하도록 활성화하라.
  - (선택) `ThreadPoolTaskExecutor`를 빈(Bean)으로 등록하여 스레드 풀 사이즈를 명시적으로 관리하는 것을 권장한다.

  ```java
  @Configuration
  @EnableAsync
  public class AsyncConfig {
      // 필요 시 ThreadPoolTaskExecutor Bean 등록
  }
  ```

## 🧩 Phase 13: ObjectMapper Bean 의존성 주입(DI) 적용 (Issue 2)

`ItemService`에서 `ObjectMapper`를 `new` 키워드로 직접 생성하여 사용하고 있다. 이는 `RedisConfig` 등에 설정해 둔 `JavaTimeModule`(날짜 직렬화) 등의 글로벌 설정을 모조리 무시하는 안티 패턴이다.

- **대상 파일**: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
- **지시사항**:
  - `private final ObjectMapper objectMapper = new ObjectMapper();` 코드를 삭제하라.
  - 롬복(Lombok)의 @RequiredArgsConstructor를 활용하여 스프링 컨테이너가 관리하는 ObjectMapper 빈(Bean)을 생성자 주입(Constructor Injection) 방식으로 할당받도록 수정하라.

```java
private final ObjectMapper objectMapper; // 생성자 주입으로 변경
```

## ✂️ Phase 14: 레거시 데이터 파싱 시 공백(Trim) 처리 보강 (Issue 3)

이미지 URL 같은 문자열 데이터를 콤마(,)로 분리할 때, 띄어쓰기가 포함된 경우("a, b") 배열에 공백이 그대로 들어가는 버그가 있다.

- 대상 파일: `src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java` (187-193 라인 부근)
- 지시사항:
  - `return List.of(content.split(","));` 코드를 수정하라.
  - 정규식을 사용하여 콤마 주변의 공백을 한 번에 제거하거나(`split("\\s*,\\s*")`), Stream API의 trim()을 활용하여 쓰레기 데이터가 들어가지 않도록 방어 로직을 구축하라.

```java
// 수정 예시
return Arrays.stream(content.split(","))
.map(String::trim)
.filter(s -> !s.isEmpty())
.toList();
```

## 🛑 Phase 15: HTTP 인증/인가 상태 코드 표준화 (Issue 4)

현재 `AuthArgumentResolver`에서 Bearer 토큰이 없거나 형식이 잘못된 경우 `ExceptionCode.FORBIDDEN_REQUEST(403)`를 던지고 있다.
RFC 표준에 따르면 '인증 실패(Authentication)'는 401 Unauthorized, '권한 부족(Authorization)'이 403 Forbidden이어야 한다.

- 대상 파일: `src/main/java/com/sparta/spartatigers/global/aop/AuthArgumentResolver.java`
- 지시사항:
  - 토큰 검증 실패 시 던지는 예외를 `FORBIDDEN_REQUEST(403)`에서 `UNAUTHORIZED_REQUEST(401)` 또는 그에 준하는 인증 전용 예외 코드로 변경하라.
  - 만약 `ExceptionCode` Enum에 401 에러 코드가 없다면, `UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED, "인증이 필요하거나 유효하지 않은 토큰입니다.")` 형식으로 새로 추가한 뒤 적용하라.

## 🎯 최종 확인 지시

이 4가지 작업(Phase 12~15)은 시스템의 기본기를 바로잡는 핵심 리팩토링이다.
수정을 완료한 후 컴파일 에러가 없는지 확인하고, `[아키텍처 및 품질 결함 4건 수정 완료]` 메시지를 출력하라.
