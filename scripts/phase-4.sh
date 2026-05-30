#!/bin/bash
# =============================================================================
# Phase 4: 최종 검증 및 정리
# - 의존성 위반 검사
# - 테스트 및 빌드 검증
# - 코드 포매팅
# - 빈 디렉토리 제거
# =============================================================================
set -e

BASE="src/main/java/com/sparta/spartatigers"

echo "=========================================="
echo "Phase 4: 최종 정리 시작"
echo "=========================================="

echo "[4-1] 빈 디렉토리 제거"
find $BASE/domain -type d -empty -print -delete
echo "  ✅ 빈 디렉토리 제거 완료"

echo "[4-2] 의존성 룰 검사 (ArchUnit)"
./gradlew test --tests "com.sparta.spartatigers.architecture.ArchitectureTest"

echo "[4-3] Spotless 코드 포매팅"
./gradlew spotlessApply

echo "[4-4] 최종 컴파일 및 테스트 검증"
./scripts/verify.sh

echo ""
echo "🎉 모든 리팩토링 단계가 완료되었습니다!"
echo "이제 PR을 생성할 수 있습니다."
