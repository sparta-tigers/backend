#!/bin/bash
# =============================================================================
# Phase 3: Core 계층 구축
# - 지연 파일 5개 수용 (ExchangeChat* 4, OrphanImageCleanupScheduler 1)
# - C 항목 다수: 스크립트는 이동+치환만, 코드 수정은 사람
# =============================================================================
set -e

BASE="src/main/java/com/sparta/spartatigers"

echo "=========================================="
echo "Phase 3: Core 계층 구축 시작"
echo "=========================================="

# ──────────────────────────────────────────────
# 3-1. matchAttendance → core/attendance/
# ──────────────────────────────────────────────
echo "[3-1] matchAttendance → core/attendance/"
mkdir -p $BASE/domain/core/attendance

cp -r $BASE/domain/matchAttendance/* $BASE/domain/core/attendance/
rm -rf $BASE/domain/matchAttendance

# package/import 치환
find $BASE/domain/core/attendance -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.matchAttendance\./package com.sparta.spartatigers.domain.core.attendance./g' {} +
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.matchAttendance\./com.sparta.spartatigers.domain.core.attendance./g' {} +

echo "  ✅ matchAttendance 12개 이동 완료"

# ──────────────────────────────────────────────
# 3-2. item + exchangerequest → core/trade/
# ──────────────────────────────────────────────
echo "[3-2] item + exchangerequest → core/trade/"
mkdir -p $BASE/domain/core/trade/{controller,dto,event,model,repository,service,scheduler}

# ItemLocationEventListener → support/chat/event/ (B-5 관련)
mkdir -p $BASE/domain/support/chat/event
mv $BASE/domain/item/event/ItemLocationEventListener.java $BASE/domain/support/chat/event/
sed -i 's/package com\.sparta\.spartatigers\.domain\.item\.event;/package com.sparta.spartatigers.domain.support.chat.event;/' \
    $BASE/domain/support/chat/event/ItemLocationEventListener.java
echo "  ✅ ItemLocationEventListener → support/chat/event/"

# OrphanImageCleanupScheduler → core/trade/scheduler/ (지연 수용)
mv $BASE/domain/image/scheduler/OrphanImageCleanupScheduler.java $BASE/domain/core/trade/scheduler/
sed -i 's/package com\.sparta\.spartatigers\.domain\.image\.scheduler;/package com.sparta.spartatigers.domain.core.trade.scheduler;/' \
    $BASE/domain/core/trade/scheduler/OrphanImageCleanupScheduler.java
echo "  ✅ OrphanImageCleanupScheduler → core/trade/scheduler/"

# item (EventListener, Event 제외한 나머지) → core/trade/
# event 디렉토리 남은 파일 (ItemLocationUpdatedEvent 등)
mv $BASE/domain/item/event/*.java       $BASE/domain/core/trade/event/ 2>/dev/null || true
mv $BASE/domain/item/controller/*.java  $BASE/domain/core/trade/controller/ 2>/dev/null || true
mv $BASE/domain/item/dto/*.java         $BASE/domain/core/trade/dto/ 2>/dev/null || true
mv $BASE/domain/item/model/*.java       $BASE/domain/core/trade/model/ 2>/dev/null || true
mv $BASE/domain/item/repository/*.java  $BASE/domain/core/trade/repository/ 2>/dev/null || true
mv $BASE/domain/item/service/*.java     $BASE/domain/core/trade/service/ 2>/dev/null || true

# exchangerequest → core/trade/
mv $BASE/domain/exchangerequest/controller/*.java   $BASE/domain/core/trade/controller/ 2>/dev/null || true
mv $BASE/domain/exchangerequest/dto/request/*.java  $BASE/domain/core/trade/dto/ 2>/dev/null || true
mv $BASE/domain/exchangerequest/dto/response/*.java $BASE/domain/core/trade/dto/ 2>/dev/null || true
mv $BASE/domain/exchangerequest/model/*.java        $BASE/domain/core/trade/model/ 2>/dev/null || true
mv $BASE/domain/exchangerequest/repository/*.java   $BASE/domain/core/trade/repository/ 2>/dev/null || true
mv $BASE/domain/exchangerequest/service/*.java      $BASE/domain/core/trade/service/ 2>/dev/null || true

echo "  ✅ item + exchangerequest → core/trade/ 이동 완료"

# package 선언 치환
find $BASE/domain/core/trade -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.item\./package com.sparta.spartatigers.domain.core.trade./g' {} +
find $BASE/domain/core/trade -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.exchangerequest\./package com.sparta.spartatigers.domain.core.trade./g' {} +

# import 치환
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.item\./com.sparta.spartatigers.domain.core.trade./g' {} +
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.exchangerequest\./com.sparta.spartatigers.domain.core.trade./g' {} +

# ──────────────────────────────────────────────
# 3-3. directRoom → core/direct/
# + 지연 파일 4개 수용 (ExchangeChat*, RedisDirectMessage*)
# ──────────────────────────────────────────────
echo "[3-3] directRoom + 지연 파일 → core/direct/"
mkdir -p $BASE/domain/core/direct/{controller,dto/request,dto/response,event,model,pubsub,repository,service}

# directRoom (RedisUserSessionRegistry 제외 = Phase 2에서 처리 완료)
mv $BASE/domain/directRoom/controller/*.java   $BASE/domain/core/direct/controller/ 2>/dev/null || true
mv $BASE/domain/directRoom/dto/request/*.java  $BASE/domain/core/direct/dto/request/ 2>/dev/null || true
mv $BASE/domain/directRoom/dto/response/*.java $BASE/domain/core/direct/dto/response/ 2>/dev/null || true
mv $BASE/domain/directRoom/model/*.java        $BASE/domain/core/direct/model/ 2>/dev/null || true
mv $BASE/domain/directRoom/pubsub/*.java       $BASE/domain/core/direct/pubsub/ 2>/dev/null || true
mv $BASE/domain/directRoom/repository/*.java   $BASE/domain/core/direct/repository/ 2>/dev/null || true
mv $BASE/domain/directRoom/service/*.java      $BASE/domain/core/direct/service/ 2>/dev/null || true

# 지연 파일 수용 (stompchat → core/direct)
mv $BASE/domain/stompchat/controller/ExchangeChatController.java $BASE/domain/core/direct/controller/
mv $BASE/domain/stompchat/service/ExchangeChatService.java       $BASE/domain/core/direct/service/
mv $BASE/domain/stompchat/pubsub/RedisDirectMessagePublisher.java $BASE/domain/core/direct/pubsub/
mv $BASE/domain/stompchat/pubsub/RedisDirectMessageSubscriber.java $BASE/domain/core/direct/pubsub/

echo "  ✅ directRoom + 지연 파일 이동 완료"

# package 선언 치환
find $BASE/domain/core/direct -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.directRoom\./package com.sparta.spartatigers.domain.core.direct./g' {} +
find $BASE/domain/core/direct -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.stompchat\./package com.sparta.spartatigers.domain.core.direct./g' {} +

# import 치환
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.directRoom\./com.sparta.spartatigers.domain.core.direct./g' {} +

# 지연 파일 내부의 stompchat 참조 → 적절한 목적지로 치환
# ExchangeChatService: stompchat.pubsub.RedisDirectMessagePublisher → core.direct.pubsub
find $BASE/domain/core/direct -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.stompchat\.pubsub\.RedisDirectMessage/com.sparta.spartatigers.domain.core.direct.pubsub.RedisDirectMessage/g' {} +
# ExchangeChatController: stompchat.service.ExchangeChatService → core.direct.service
find $BASE/domain/core/direct -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.stompchat\.service\.ExchangeChatService/com.sparta.spartatigers.domain.core.direct.service.ExchangeChatService/g' {} +
# ExchangeChatController: stompchat.interceptor.StompPrincipal → support.chat.interceptor (Phase 2에서 이동됨)
find $BASE/domain/core/direct -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.stompchat\.interceptor\./com.sparta.spartatigers.domain.support.chat.interceptor./g' {} +

echo "  ✅ direct import 치환 완료"

# ──────────────────────────────────────────────
# 3-4. ticketalarm → core/ticketalarm/
# ──────────────────────────────────────────────
echo "[3-4] ticketalarm → core/ticketalarm/"
mkdir -p $BASE/domain/core/ticketalarm

cp -r $BASE/domain/ticketalarm/* $BASE/domain/core/ticketalarm/
rm -rf $BASE/domain/ticketalarm

find $BASE/domain/core/ticketalarm -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.ticketalarm\./package com.sparta.spartatigers.domain.core.ticketalarm./g' {} +
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.ticketalarm\./com.sparta.spartatigers.domain.core.ticketalarm./g' {} +

echo "  ✅ ticketalarm 11개 이동 완료"

# ──────────────────────────────────────────────
# 3-5. 원본 디렉토리 삭제
# ──────────────────────────────────────────────
echo "[3-5] 원본 디렉토리 삭제"
rm -rf $BASE/domain/item
rm -rf $BASE/domain/exchangerequest
rm -rf $BASE/domain/directRoom
rm -rf $BASE/domain/stompchat
rm -rf $BASE/domain/image

echo ""
echo "=========================================="
echo "Phase 3 완료"
echo "=========================================="
echo ""
echo "📌 C 항목 수동 처리 필요 (스크립트 실행 후):"
echo ""
echo "  [C-3] TradeAcceptedEvent 신규 생성:"
echo "    - 경로: domain/core/trade/event/TradeAcceptedEvent.java"
echo "    - ExchangeRequestService에서 DirectRoomService 직접 호출 제거"
echo "    - ApplicationEventPublisher로 이벤트 발행 전환"
echo ""
echo "  [C-4] DirectRoom JPA 연관관계 수정:"
echo "    - domain/core/direct/model/DirectRoom.java"
echo "    - @ManyToOne ExchangeRequest → Long exchangeRequestId"
echo ""
echo "  [C-4] TradeAcceptedEventListener 신규 생성:"
echo "    - 경로: domain/core/direct/event/TradeAcceptedEventListener.java"
echo ""
echo "  [B-5] LocationService.calculateDistance 파라미터 수정:"
echo "    - domain/support/chat/service/LocationService.java"
echo "    - Item 파라미터 → (Double itemLat, Double itemLon)"
echo ""
echo "수동 처리 완료 후 실행:"
echo "  ./scripts/verify.sh"
echo "  git add -A && git commit -m 'refactor: Phase 3 Core 계층 구축'"
echo "  git tag phase-3-done"
