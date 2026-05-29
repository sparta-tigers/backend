# Phase 0: 사전 준비 (리스크 제거)

> **목표:** 코드를 한 줄도 이동하기 전에, 안전한 롤백 경로와 자동화된 검증 수단을 확보한다.

---

## 0-1. 브랜치 전략

```bash
git checkout -b refactor/package-restructure
```

- 모든 Phase는 이 단일 브랜치에서 진행한다.
- 각 Phase 완료 시 `Phase X done` 태그를 남겨 롤백 지점을 확보한다.

```bash
# Phase 1 완료 후
git tag phase-1-done
```

---

## 0-2. 테스트 기준선 확보

리팩토링 전 전체 테스트가 Green인지 확인하고, 결과를 기록한다.

```bash
./gradlew clean test 2>&1 | tee docs/test-baseline.log
```

- 실패하는 테스트가 있으면 **리팩토링 시작 전에 반드시 수정**하거나, 의도적 skip이라면 목록을 명시한다.
- 이 기준선 로그는 Phase 4 검증 시 비교 대상이 된다.

---

## 0-3. 마이그레이션 전략 결정

### IDE Refactor > Move (권장)

- IntelliJ의 `Refactor > Move Package/Class` 기능은 import 경로를 자동으로 갱신한다.
- **주의:** 리소스 파일(`application.yml`, Flyway 마이그레이션 등)의 패키지 경로 참조는 자동 갱신되지 않으므로 수동 확인이 필요하다.

### sed 기반 일괄 치환 (보조)

IDE Refactor가 놓치는 경우(문자열 리터럴, 설정 파일)를 위한 보조 스크립트:

```bash
# 예시: auth 패키지 경로 치환 (dry-run)
grep -r "com.sparta.spartatigers.domain.auth" src/ --include="*.java" -l
```

---

## 0-4. 컴파일/린트 검증 스크립트

매 Phase 완료 시 실행할 원커맨드 검증:

```bash
#!/bin/bash
# scripts/verify.sh
set -e

echo "=== Spotless Check ==="
./gradlew spotlessCheck

echo "=== Compile ==="
./gradlew compileJava

echo "=== Test ==="
./gradlew test

echo "✅ All checks passed."
```

---

## 0-5. 영향 범위 사전 점검

### Flyway 마이그레이션

- 패키지 이동은 DB 스키마에 영향을 주지 않는다 (JPA 엔티티의 `@Table`, `@Column` 등은 패키지와 무관).
- 단, `@Entity` 스캔 경로가 `application.yml` 또는 `JpaConfig`에 하드코딩되어 있다면 확인 필요.

### application.yml / Config 클래스

- `WebSocketConfig`에서 `StompInterceptor` import 경로 변경 필요.
- `RedisConfig`에서 subscriber 빈 등록 경로 변경 필요.
- `OauthConfig`, `JwtConfig` 등이 auth 패키지를 참조하는 경우 경로 변경 필요.

### QueryDSL Q-Class 재생성

```bash
./gradlew clean compileJava
# Q클래스는 컴파일 시 자동 재생성되므로 패키지 이동 후 clean build 필수
```

---

## 0-6. Phase 0 완료 체크리스트

- [ ] `refactor/package-restructure` 브랜치 생성
- [ ] 전체 테스트 Green 확인 및 기준선 로그 저장
- [ ] `scripts/verify.sh` 스크립트 생성
- [ ] `application.yml` / Config 클래스 내 하드코딩 패키지 경로 목록 파악
- [ ] 팀원에게 리팩토링 시작 공유 (해당 브랜치에서 다른 기능 개발 중단)
- [ ] 메인 Application 클래스 및 Config 파일 내 @ComponentScan, @EntityScan 하드코딩 문자열 검색 및 목록화
