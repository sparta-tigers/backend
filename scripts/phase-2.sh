#!/bin/bash
# =============================================================================
# Phase 2: Support 계층 구축
# - SED-1: 지연 파일 4개 (ExchangeChat*, RedisDirectMessage*) sed 제외
# - SED-2: OrphanImageCleanupScheduler sed 제외
# =============================================================================
set -e

BASE="src/main/java/com/sparta/spartatigers"

# Phase 3 지연 파일 목록 (sed 제외 대상)
EXCLUDE_STOMPCHAT=(
    "*/stompchat/controller/ExchangeChatController.java"
    "*/stompchat/service/ExchangeChatService.java"
    "*/stompchat/pubsub/RedisDirectMessagePublisher.java"
    "*/stompchat/pubsub/RedisDirectMessageSubscriber.java"
)
EXCLUDE_OPTS=""
for f in "${EXCLUDE_STOMPCHAT[@]}"; do
    EXCLUDE_OPTS="$EXCLUDE_OPTS -not -path $f"
done

echo "=========================================="
echo "Phase 2: Support 계층 구축 시작"
echo "=========================================="

# ──────────────────────────────────────────────
# 2-1. stompchat → support/chat/
# ──────────────────────────────────────────────
echo "[2-1] stompchat → support/chat/"
mkdir -p $BASE/domain/support/chat/{controller,dto/request,dto/response,eventlistener,interceptor,model,pubsub,repository,service,registry}

# stompchat 이동 대상 (16개 — 지연 4개 제외)
mv $BASE/domain/stompchat/controller/LiveBoardChatController.java $BASE/domain/support/chat/controller/
mv $BASE/domain/stompchat/controller/LocationController.java      $BASE/domain/support/chat/controller/

mv $BASE/domain/stompchat/dto/request/LocationRequestDto.java     $BASE/domain/support/chat/dto/request/
mv $BASE/domain/stompchat/dto/response/RedisUpdateDto.java        $BASE/domain/support/chat/dto/response/

mv $BASE/domain/stompchat/eventlistener/WebSocketEventListener.java $BASE/domain/support/chat/eventlistener/
mv $BASE/domain/stompchat/interceptor/StompInterceptor.java         $BASE/domain/support/chat/interceptor/
mv $BASE/domain/stompchat/interceptor/StompPrincipal.java           $BASE/domain/support/chat/interceptor/

mv $BASE/domain/stompchat/model/ChatDomainType.java  $BASE/domain/support/chat/model/
mv $BASE/domain/stompchat/model/ChatMessage.java     $BASE/domain/support/chat/model/

mv $BASE/domain/stompchat/pubsub/RedisChatPublisher.java    $BASE/domain/support/chat/pubsub/
mv $BASE/domain/stompchat/pubsub/RedisChatSubscriber.java   $BASE/domain/support/chat/pubsub/
mv $BASE/domain/stompchat/pubsub/RedisLocationPublisher.java $BASE/domain/support/chat/pubsub/
mv $BASE/domain/stompchat/pubsub/RedisLocationSubscriber.java $BASE/domain/support/chat/pubsub/

mv $BASE/domain/stompchat/repository/LiveBoardChatRepository.java $BASE/domain/support/chat/repository/
mv $BASE/domain/stompchat/service/ChatService.java       $BASE/domain/support/chat/service/
mv $BASE/domain/stompchat/service/LocationService.java   $BASE/domain/support/chat/service/

echo "  ✅ stompchat 16개 파일 이동 완료"

# LiveBoardMatchSubscriber 이관 (liveboard → support/chat/pubsub)
echo "[2-1] LiveBoardMatchSubscriber → support/chat/pubsub/"
mv $BASE/domain/liveboard/pubsub/LiveBoardMatchSubscriber.java $BASE/domain/support/chat/pubsub/
echo "  ✅ LiveBoardMatchSubscriber 이동 완료"

# RedisUserSessionRegistry 이관 (directRoom → support/chat/registry)
echo "[2-1] RedisUserSessionRegistry → support/chat/registry/"
mv $BASE/domain/directRoom/registry/RedisUserSessionRegistry.java $BASE/domain/support/chat/registry/
echo "  ✅ RedisUserSessionRegistry 이동 완료"

# package 선언 치환 (이동된 파일들)
find $BASE/domain/support/chat -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.stompchat\./package com.sparta.spartatigers.domain.support.chat./g' {} +
# LiveBoardMatchSubscriber의 package (liveboard.pubsub → support.chat.pubsub)
sed -i 's/package com\.sparta\.spartatigers\.domain\.liveboard\.pubsub;/package com.sparta.spartatigers.domain.support.chat.pubsub;/' \
    $BASE/domain/support/chat/pubsub/LiveBoardMatchSubscriber.java
# RedisUserSessionRegistry의 package (directRoom.registry → support.chat.registry)
sed -i 's/package com\.sparta\.spartatigers\.domain\.directRoom\.registry;/package com.sparta.spartatigers.domain.support.chat.registry;/' \
    $BASE/domain/support/chat/registry/RedisUserSessionRegistry.java

# [SED-1] 전체 프로젝트 import 치환 (지연 파일 4개 제외)
echo "[2-1] stompchat import 일괄 치환 (지연 파일 제외)"
find $BASE -name '*.java' \
    -not -path "*/stompchat/controller/ExchangeChatController.java" \
    -not -path "*/stompchat/service/ExchangeChatService.java" \
    -not -path "*/stompchat/pubsub/RedisDirectMessagePublisher.java" \
    -not -path "*/stompchat/pubsub/RedisDirectMessageSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.stompchat\./com.sparta.spartatigers.domain.support.chat./g' {} +

# LiveBoardMatchSubscriber import 치환 (liveboard.pubsub → support.chat.pubsub)
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.liveboard\.pubsub\.LiveBoardMatchSubscriber/com.sparta.spartatigers.domain.support.chat.pubsub.LiveBoardMatchSubscriber/g' {} +

# RedisUserSessionRegistry import 치환 (directRoom.registry → support.chat.registry)
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.directRoom\.registry\.RedisUserSessionRegistry/com.sparta.spartatigers.domain.support.chat.registry.RedisUserSessionRegistry/g' {} +

echo "  ✅ stompchat import 치환 완료"

# ──────────────────────────────────────────────
# 2-3. image → support/image/
# [SED-2] OrphanImageCleanupScheduler 제외
# ──────────────────────────────────────────────
echo "[2-3] image → support/image/"
mkdir -p $BASE/domain/support/image/{controller,service}

mv $BASE/domain/image/controller/ImageController.java          $BASE/domain/support/image/controller/
mv $BASE/domain/image/service/ImageStorageService.java         $BASE/domain/support/image/service/
mv $BASE/domain/image/service/LocalImageStorageServiceImpl.java $BASE/domain/support/image/service/

# package 선언 치환
find $BASE/domain/support/image -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.image\./package com.sparta.spartatigers.domain.support.image./g' {} +

# [SED-2] import 치환 (OrphanImageCleanupScheduler 제외)
find $BASE -name '*.java' \
    -not -path "*/image/scheduler/OrphanImageCleanupScheduler.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.image\./com.sparta.spartatigers.domain.support.image./g' {} +

echo "  ✅ image 3개 파일 이동 완료"

# ──────────────────────────────────────────────
# 2-4. weather → support/weather/
# ──────────────────────────────────────────────
echo "[2-4] weather → support/weather/"
mkdir -p $BASE/domain/support/weather

# weather 전체 이동 (하위 디렉토리 구조 유지)
cp -r $BASE/domain/weather/* $BASE/domain/support/weather/
rm -rf $BASE/domain/weather

# package 선언 치환
find $BASE/domain/support/weather -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.weather\./package com.sparta.spartatigers.domain.support.weather./g' {} +

# import 치환
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.weather\./com.sparta.spartatigers.domain.support.weather./g' {} +

echo "  ✅ weather 이동 완료"

# ──────────────────────────────────────────────
# 2-5. 원본 디렉토리 정리
# ──────────────────────────────────────────────
echo "[2-5] 원본 디렉토리 정리"
rm -rf $BASE/domain/liveboard

echo "  📌 잔존 확인 - domain/stompchat/:"
find $BASE/domain/stompchat -name '*.java' -type f 2>/dev/null || echo "  (없음)"
echo "  📌 잔존 확인 - domain/image/:"
find $BASE/domain/image -name '*.java' -type f 2>/dev/null || echo "  (없음)"

echo ""
echo "=========================================="
echo "Phase 2 완료"
echo "=========================================="
echo ""
echo "수동 처리 없음 (Phase 2 B/C 없음)"
echo ""
echo "실행:"
echo "  ./scripts/verify.sh"
echo "  git add -A && git commit -m 'refactor: Phase 2 Support 계층 구축'"
echo "  git tag phase-2-done"
