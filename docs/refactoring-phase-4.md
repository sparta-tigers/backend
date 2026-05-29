# Phase 4: 최종 검증 및 정리

> **목표:** 모든 도메인의 이동이 완료된 후, 전체 시스템이 정상 동작하는지 검증하고 코드베이스를 정리한다. 의존성 룰이 지켜지고 있는지 확인하는 스크립트를 확립한다.

---

## 4-1. 의존성 검증 스크립트 작성 (ArchUnit 대체)

ArchUnit을 도입하지 않더라도, 쉘 스크립트로 CI/CD 파이프라인에서 의존성 위반을 잡아낼 수 있다.

`scripts/check-dependencies.sh` 생성:

```bash
#!/bin/bash
set -e

echo "🔍 의존성 방향(Dependency Flow) 검사 중..."

SRC_DIR="src/main/java/com/sparta/spartatigers"
FAIL_COUNT=0

# 1. Foundation → Core 역참조 검사
if grep -rq "domain\.core\." $SRC_DIR/domain/foundation/ --include="*.java" 2>/dev/null; then
    echo "❌ Foundation에서 Core를 참조하는 위반이 발견되었습니다."
    grep -rn "domain\.core\." $SRC_DIR/domain/foundation/ --include="*.java"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# 2. Foundation → Support 역참조 검사
if grep -rq "domain\.support\." $SRC_DIR/domain/foundation/ --include="*.java" 2>/dev/null; then
    echo "❌ Foundation에서 Support를 참조하는 위반이 발견되었습니다."
    grep -rn "domain\.support\." $SRC_DIR/domain/foundation/ --include="*.java"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# 3. Support → Core 역참조 검사 (DTO, Model, Event는 예외 허용하고 Service/Repository/Controller 통제)
if grep -rq "domain\.core\..*\.\(repository\|service\|controller\)\." $SRC_DIR/domain/support/ --include="*.java" 2>/dev/null; then
    echo "❌ Support에서 Core의 Service/Repository/Controller를 참조하는 위반이 발견되었습니다."
    grep -rn "domain\.core\..*\.\(repository\|service\|controller\)\." $SRC_DIR/domain/support/ --include="*.java"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# 4. Core 내부 도메인 간 교차 참조 검사 (자기 자신 제외, DTO/Model/Event 허용, Service/Repository/Controller 통제)
CORE_DOMAINS=("attendance" "trade" "direct" "ticketalarm")
for domain in "${CORE_DOMAINS[@]}"; do
    for target in "${CORE_DOMAINS[@]}"; do
        if [ "$domain" != "$target" ]; then
            if grep -rq "domain\.core\.$target\.\(repository\|service\|controller\)\." $SRC_DIR/domain/core/$domain/ --include="*.java" 2>/dev/null; then
                echo "❌ Core 내부 직접 참조 위반 (Service/Repository/Controller 교차 호출 금지): $domain -> $target"
                grep -rn "domain\.core\.$target\.\(repository\|service\|controller\)\." $SRC_DIR/domain/core/$domain/ --include="*.java"
                FAIL_COUNT=$((FAIL_COUNT + 1))
            fi
        fi
    done
done

if [ $FAIL_COUNT -gt 0 ]; then
    echo "🚨 총 $FAIL_COUNT 건의 의존성 위반이 발견되었습니다. 빌드를 실패처리합니다."
    exit 1
else
    echo "✅ 모든 의존성 규칙이 준수되었습니다."
    exit 0
fi
```

실행 권한 부여:
```bash
chmod +x scripts/check-dependencies.sh
```

---

## 4-2. 최종 테스트 및 린트 검증

Phase 0에서 확보한 `scripts/verify.sh`와 결합하여 최종 검증:

```bash
./scripts/check-dependencies.sh
./scripts/verify.sh
```

- 모든 테스트가 통과하는지 확인.
- `spotlessCheck`가 통과하는지 확인. (패키지 이동 후 import 순서가 꼬였을 수 있으므로 `./gradlew spotlessApply` 실행)

---

## 4-3. 불필요한 import 제거 및 정리

IDE의 기능을 활용하여 전체 프로젝트에서 안 쓰는 import를 제거한다.
- IntelliJ: `Optimize Imports` (전체 프로젝트 대상)

---

## 4-4. Phase 4 완료 및 머지 준비

- [ ] `scripts/check-dependencies.sh` 스크립트 통과 확인
- [ ] 전체 테스트 통과 확인 (Phase 0의 Baseline과 비교)
- [ ] Spotless 포맷팅 적용 완료
- [ ] 더미/임시 디렉토리 및 남겨진 빈 디렉토리 모두 제거 확인
- [ ] `git tag phase-4-done`
- [ ] PR 생성: "Refactor: Backend Package Restructure based on 3-Tier Architecture"
