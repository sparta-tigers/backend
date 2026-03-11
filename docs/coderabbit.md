# Coderabbit PR Review

> @coderabbit.md 문서는 coderabbit이 PR review한 후 코멘트 남겨준거야.
> 이 내용은 설계자(Gemini 3.1 Pro)에게 전달하여 명확한 작업지시서를 받을거야.
> 너는 첨부한 문서를 기반으로 현재 프로젝트 상황을 명확히 설계자에게 전달하는 것이 목적이야.
> 절대 너의 생각, 의견을 넣지 말고, 반드시 현재 상황을 명확히 설계자에게 전달하는 것을 목표로 해.
> docs 디렉토리에 상황보고서 마크다운 문서 생성해줘.

## @EnableAsync 설정이 누락되어 있습니다. 이로 인해 @Async 어노테이션이 동작하지 않습니다

@Async 어노테이션이 비동기 실행되려면 설정 클래스에 @EnableAsync가 선언되어야 하는데, 현재 프로젝트에서는 이 설정을 찾을 수 없습니다. 또한, LocationService.notifyUsersNearBy()가 내부적으로 모든 예외를 catch하고 로그만 남기므로, 여기서의 catch 블록은 LocationService에서 발생한 예외를 받을 수 없습니다.

### 🤖 Prompt for AI Agents 1

```text
Verify each finding against the current code and only fix it if needed.

In
`@src/main/java/com/sparta/spartatigers/domain/item/event/ItemLocationEventListener.java`
around lines 20 - 30, Add `@EnableAsync` to your Spring configuration (e.g., the
main application class or a `@Configuration` class) so `@Async` on
ItemLocationEventListener.handleItemLocationUpdated is honored; then update
LocationService.notifyUsersNearBy to stop swallowing exceptions (either remove
its broad try/catch or rethrow/log-and-throw) so exceptions can propagate to the
listener's catch for proper handling and logging. Ensure the change references
the ItemLocationEventListener.handleItemLocationUpdated method and the
LocationService.notifyUsersNearBy method so the async behavior and exception
flow work as intended.
```

## Spring 관리 ObjectMapper Bean을 주입받아 사용하세요

`new ObjectMapper()`로 직접 생성하면 RedisConfig에서 설정한 `JavaTimeModule` 및 날짜 직렬화 설정이 적용되지 않습니다.
`ReadItemResponseDto`에 날짜/시간 필드가 포함되면 직렬화 결과가 달라질 수 있습니다.

### 🐛 수정 제안

```java
 `@RequiredArgsConstructor`
 public class ItemService {

     private static final double SEARCH_RADIUS_KM = 0.05;
     private final ItemRepository itemRepository;
     private final UserRepository userRepository;
     private final ApplicationEventPublisher applicationEventPublisher;
     private final LocationService locationService;
-    private final ObjectMapper objectMapper = new ObjectMapper();
+    private final ObjectMapper objectMapper;
```

`@RequiredArgsConstructor`가 생성자 주입을 처리하므로, Spring 컨텍스트의 ObjectMapper Bean이 자동으로 주입됩니다.

### 📝 Committable suggestion

> ‼️ IMPORTANT
> Carefully review the code before committing. Ensure that it accurately replaces the highlighted code, contains no missing lines, and has no issues with indentation. Thoroughly test & benchmark the code to ensure it meets the requirements.

#### Suggested change

```java
- private final ObjectMapper objectMapper = new ObjectMapper();
+ private final ObjectMapper objectMapper;
```

### 🤖 Prompt for AI Agents 2

```text
Verify each finding against the current code and only fix it if needed.

In `@src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
at line 44, The ItemService currently instantiates a new ObjectMapper (private
final ObjectMapper objectMapper = new ObjectMapper()), which bypasses Spring's
configured ObjectMapper (e.g., JavaTimeModule and date serialization from
RedisConfig); replace the direct instantiation with constructor-injected Spring
bean by removing the "= new ObjectMapper()" and relying on the existing
`@RequiredArgsConstructor` to inject the framework-managed ObjectMapper into
ItemService so serialization of types like ReadItemResponseDto uses the app-wide
configuration.
```

## 레거시 데이터 파싱 시 공백 처리가 누락되었습니다

`content.split(",")` 결과에 앞뒤 공백이 포함될 수 있습니다. 예: "[a, b]" → ["a", " b"]. 공백 trim 처리를 추가하는 것이 안전합니다.

### 🐛 수정 제안

```java
-                return List.of(content.split(","));
+                return java.util.Arrays.stream(content.split(","))
+                    .map(String::trim)
+                    .toList();
```

### 🤖 Prompt for AI Agents 3

```text
Verify each finding against the current code and only fix it if needed.

In `@src/main/java/com/sparta/spartatigers/domain/item/service/ItemService.java`
around lines 187 - 193, The parsing of legacy imageUrlsJson in ItemService
currently splits the inner content with content.split(",") but does not trim
each element, causing entries like " b" to remain; update the code that handles
imageUrlsJson (the block using imageUrlsJson.startsWith("[") &&
imageUrlsJson.endsWith("]") and the returned List.of(content.split(","))) to
trim whitespace from each split element and filter out empty strings before
returning (e.g., map each token to token.trim() and exclude blanks), ensuring
the returned List contains clean URLs.
```

## 대소문자 무시 비교가 적용되었으나 HTTP 상태 코드가 부적절합니다

RFC 7235/6750에 따른 대소문자 무시 비교가 `toLowerCase().startsWith("bearer ")`로 구현되었습니다. 기능적으로는 정확하지만, 약간의 성능 개선을 위해 `regionMatches(true, 0, "Bearer ", 0, 7)`를 고려해볼 수 있습니다.

더 중요한 점은, `ExceptionCode.FORBIDDEN_REQUEST`가 HTTP 403을 반환하는데, 이는 인증 실패에 대해 RFC 7235가 규정하는 401 Unauthorized가 아닙니다.
Authorization 헤더가 없거나 잘못된 경우 401을 반환하고, 유효한 인증 후 권한이 없을 때만 403을 반환해야 합니다.

### 🔧 제안: 인증 실패 시 401 반환

`ExceptionCode`에 인증 실패 전용 코드를 추가하거나 기존 `UNAUTHORIZED` 또는 `NOT_FOUND_JWT`를 사용하는 것을 고려하세요:

```java
-        if (bearerToken == null || bearerToken.isBlank() || !bearerToken.toLowerCase().startsWith("bearer ")) {
-            throw new InvalidRequestException(ExceptionCode.FORBIDDEN_REQUEST);
+        if (bearerToken == null || bearerToken.isBlank() || !bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
+            throw new InvalidRequestException(ExceptionCode.UNAUTHORIZED);
         }
```

### 🤖 Prompt for AI Agents 4

```text
Verify each finding against the current code and only fix it if needed.

In `@src/main/java/com/sparta/spartatigers/global/aop/AuthArgumentResolver.java`
around lines 38 - 40, The Authorization header check in AuthArgumentResolver
currently lowercases the header and throws ExceptionCode.FORBIDDEN_REQUEST on
missing/invalid tokens; change this to throw an authentication error (e.g.,
ExceptionCode.UNAUTHORIZED or a new NOT_AUTHENTICATED code) so a 401 is returned
for missing/invalid Authorization headers and reserve FORBIDDEN for post-authz
failures, and optionally replace bearerToken.toLowerCase().startsWith("bearer ")
with bearerToken.regionMatches(true, 0, "Bearer ", 0, 7) for case-insensitive
prefix matching to avoid creating a new string; update the throw site
referencing bearerToken and ExceptionCode.FORBIDDEN_REQUEST in
AuthArgumentResolver accordingly.
```
