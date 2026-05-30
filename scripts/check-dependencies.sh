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
if grep -Erq "domain\.core\..*\.(repository|service|controller)\." $SRC_DIR/domain/support/ --include="*.java" 2>/dev/null; then
    echo "❌ Support에서 Core의 Service/Repository/Controller를 참조하는 위반이 발견되었습니다."
    grep -Ern "domain\.core\..*\.(repository|service|controller)\." $SRC_DIR/domain/support/ --include="*.java"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# 4. Core 내부 도메인 간 교차 참조 검사 (자기 자신 제외, DTO/Model/Event 허용, Service/Repository/Controller 통제)
CORE_DOMAINS=("attendance" "trade" "direct" "ticketalarm")
for domain in "${CORE_DOMAINS[@]}"; do
    for target in "${CORE_DOMAINS[@]}"; do
        if [ "$domain" != "$target" ]; then
            if grep -Erq "domain\.core\.$target\.(repository|service|controller)\." $SRC_DIR/domain/core/$domain/ --include="*.java" 2>/dev/null; then
                echo "❌ Core 내부 직접 참조 위반 (Service/Repository/Controller 교차 호출 금지): $domain -> $target"
                grep -Ern "domain\.core\.$target\.(repository|service|controller)\." $SRC_DIR/domain/core/$domain/ --include="*.java"
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
