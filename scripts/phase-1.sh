#!/bin/bash
# =============================================================================
# Phase 1: Foundation 계층 구축
# - 실행 전 반드시 Phase 0 완료 확인
# - SED-3: LiveBoardMatchSubscriber.java는 sed 대상에서 제외
# =============================================================================
set -e

BASE="src/main/java/com/sparta/spartatigers"

echo "=========================================="
echo "Phase 1: Foundation 계층 구축 시작"
echo "=========================================="

# ──────────────────────────────────────────────
# 1-2. TokenClaim → global/aop/
# ──────────────────────────────────────────────
echo "[1-2] TokenClaim → global/aop/"
mkdir -p $BASE/global/aop
mv $BASE/domain/auth/model/TokenClaim.java $BASE/global/aop/

# TokenClaim package 선언 수정
sed -i 's/package com\.sparta\.spartatigers\.domain\.auth\.model;/package com.sparta.spartatigers.global.aop;/' \
    $BASE/global/aop/TokenClaim.java

# 전체 프로젝트 import 치환
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.auth\.model\.TokenClaim/com.sparta.spartatigers.global.aop.TokenClaim/g' {} +

echo "  ✅ TokenClaim 이동 완료"

# ──────────────────────────────────────────────
# 1-3-1. auth → foundation/user/auth/
# ──────────────────────────────────────────────
echo "[1-3-1] auth → foundation/user/auth/"
mkdir -p $BASE/domain/foundation/user/auth/{client,controller,dto,model,repository,service}

mv $BASE/domain/auth/client/*.java     $BASE/domain/foundation/user/auth/client/
mv $BASE/domain/auth/controller/*.java $BASE/domain/foundation/user/auth/controller/
mv $BASE/domain/auth/dto/*.java        $BASE/domain/foundation/user/auth/dto/
mv $BASE/domain/auth/model/*.java      $BASE/domain/foundation/user/auth/model/
mv $BASE/domain/auth/repository/*.java $BASE/domain/foundation/user/auth/repository/
mv $BASE/domain/auth/service/*.java    $BASE/domain/foundation/user/auth/service/

# package/import 치환
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.auth\./com.sparta.spartatigers.domain.foundation.user.auth./g' {} +

echo "  ✅ auth 이동 완료"

# ──────────────────────────────────────────────
# 1-3-2. user → foundation/user/account/
# ──────────────────────────────────────────────
echo "[1-3-2] user → foundation/user/account/"
mkdir -p $BASE/domain/foundation/user/account/{controller,dto,model,repository,service}

mv $BASE/domain/user/controller/*.java $BASE/domain/foundation/user/account/controller/
mv $BASE/domain/user/dto/*.java        $BASE/domain/foundation/user/account/dto/
mv $BASE/domain/user/model/*.java      $BASE/domain/foundation/user/account/model/
mv $BASE/domain/user/repository/*.java $BASE/domain/foundation/user/account/repository/
mv $BASE/domain/user/service/*.java    $BASE/domain/foundation/user/account/service/

find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.user\./com.sparta.spartatigers.domain.foundation.user.account./g' {} +

echo "  ✅ user 이동 완료"

# ──────────────────────────────────────────────
# 1-3-3. favoriteteam → foundation/user/favoriteteam/
# ──────────────────────────────────────────────
echo "[1-3-3] favoriteteam → foundation/user/favoriteteam/"
mkdir -p $BASE/domain/foundation/user/favoriteteam/{controller,dto,model/entity,repository,service}

mv $BASE/domain/favoriteteam/controller/*.java   $BASE/domain/foundation/user/favoriteteam/controller/
mv $BASE/domain/favoriteteam/dto/*.java           $BASE/domain/foundation/user/favoriteteam/dto/
mv $BASE/domain/favoriteteam/model/entity/*.java  $BASE/domain/foundation/user/favoriteteam/model/entity/
mv $BASE/domain/favoriteteam/repository/*.java    $BASE/domain/foundation/user/favoriteteam/repository/
mv $BASE/domain/favoriteteam/service/*.java       $BASE/domain/foundation/user/favoriteteam/service/

find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.favoriteteam\./com.sparta.spartatigers.domain.foundation.user.favoriteteam./g' {} +

echo "  ✅ favoriteteam 이동 완료"

# ──────────────────────────────────────────────
# 1-4-1. team → foundation/baseball/team/
# + Stadium, StadiumRepository (liveboard에서 이관)
# ──────────────────────────────────────────────
echo "[1-4-1] team → foundation/baseball/team/"
mkdir -p $BASE/domain/foundation/baseball/team/{model,repository}

mv $BASE/domain/team/model/*.java      $BASE/domain/foundation/baseball/team/model/
mv $BASE/domain/team/repository/*.java $BASE/domain/foundation/baseball/team/repository/

# liveboard에서 Stadium 이관
mv $BASE/domain/liveboard/model/Stadium.java          $BASE/domain/foundation/baseball/team/model/
mv $BASE/domain/liveboard/repository/StadiumRepository.java $BASE/domain/foundation/baseball/team/repository/

find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.team\./com.sparta.spartatigers.domain.foundation.baseball.team./g' {} +

# Stadium import 치환 (liveboard → team)
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.Stadium/com.sparta.spartatigers.domain.foundation.baseball.team.model.Stadium/g' {} +
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.liveboard\.repository\.StadiumRepository/com.sparta.spartatigers.domain.foundation.baseball.team.repository.StadiumRepository/g' {} +

echo "  ✅ team + Stadium 이동 완료"

# ──────────────────────────────────────────────
# 1-4-2. liveboard 분해 → match + lineup
# [SED-3] LiveBoardMatchSubscriber.java 제외
# ──────────────────────────────────────────────

# --- Weather 파일 삭제 ---
echo "[1-4-2] WeatherQuery* 삭제"
rm -f $BASE/domain/liveboard/controller/WeatherQueryController.java
rm -f $BASE/domain/liveboard/service/WeatherQueryService.java
rm -f $BASE/domain/liveboard/dto/MatchWeatherResponse.java
echo "  ✅ WeatherQuery* 3개 파일 삭제 완료"

# --- Match (foundation/baseball/match/) ---
echo "[1-4-2] liveboard → foundation/baseball/match/"
mkdir -p $BASE/domain/foundation/baseball/match/{controller,dto,model,repository,service,util}

mv $BASE/domain/liveboard/controller/MatchController.java         $BASE/domain/foundation/baseball/match/controller/
mv $BASE/domain/liveboard/controller/LiveBoardRoomController.java $BASE/domain/foundation/baseball/match/controller/

mv $BASE/domain/liveboard/dto/MatchScheduleResponseDto.java    $BASE/domain/foundation/baseball/match/dto/
mv $BASE/domain/liveboard/dto/LiveBoardDataResponseDto.java    $BASE/domain/foundation/baseball/match/dto/
mv $BASE/domain/liveboard/dto/LiveBoardRoomResponseDto.java    $BASE/domain/foundation/baseball/match/dto/

mv $BASE/domain/liveboard/model/Match.java              $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/MatchResult.java        $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/MatchScore.java         $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/InningTexts.java        $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/HomeAway.java           $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/LeagueType.java         $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/LiveBoardStatus.java    $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/LiveBoardConnection.java $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/LiveBoardData.java      $BASE/domain/foundation/baseball/match/model/
mv $BASE/domain/liveboard/model/LiveBoardRoom.java      $BASE/domain/foundation/baseball/match/model/

mv $BASE/domain/liveboard/repository/MatchRepository.java              $BASE/domain/foundation/baseball/match/repository/
mv $BASE/domain/liveboard/repository/LiveBoardConnectionRepository.java $BASE/domain/foundation/baseball/match/repository/
mv $BASE/domain/liveboard/repository/LiveBoardRoomRepository.java      $BASE/domain/foundation/baseball/match/repository/

mv $BASE/domain/liveboard/service/MatchScheduleService.java  $BASE/domain/foundation/baseball/match/service/
mv $BASE/domain/liveboard/service/LiveBoardDataService.java  $BASE/domain/foundation/baseball/match/service/
mv $BASE/domain/liveboard/service/LiveBoardMatchService.java $BASE/domain/foundation/baseball/match/service/
mv $BASE/domain/liveboard/service/LiveboardRoomService.java  $BASE/domain/foundation/baseball/match/service/

mv $BASE/domain/liveboard/util/GlobalSessionGenerator.java $BASE/domain/foundation/baseball/match/util/

echo "  ✅ match 파일 이동 완료 (23개)"

# --- Lineup (foundation/baseball/lineup/) ---
echo "[1-4-2] liveboard + startinglineup → foundation/baseball/lineup/"
mkdir -p $BASE/domain/foundation/baseball/lineup/{controller,dto,model,repository,service}

# liveboard에서 이관
mv $BASE/domain/liveboard/controller/LineupController.java $BASE/domain/foundation/baseball/lineup/controller/
mv $BASE/domain/liveboard/dto/LineupBatterResponse.java    $BASE/domain/foundation/baseball/lineup/dto/
mv $BASE/domain/liveboard/dto/LineupCacheDto.java          $BASE/domain/foundation/baseball/lineup/dto/
mv $BASE/domain/liveboard/dto/LineupResponseDto.java       $BASE/domain/foundation/baseball/lineup/dto/
mv $BASE/domain/liveboard/model/LineupBatter.java          $BASE/domain/foundation/baseball/lineup/model/
mv $BASE/domain/liveboard/service/LineupQueryService.java  $BASE/domain/foundation/baseball/lineup/service/

# startinglineup에서 이관
mv $BASE/domain/startinglineup/model/LineupPlayer.java      $BASE/domain/foundation/baseball/lineup/model/
mv $BASE/domain/startinglineup/model/Position.java          $BASE/domain/foundation/baseball/lineup/model/
mv $BASE/domain/startinglineup/model/StartingLineup.java    $BASE/domain/foundation/baseball/lineup/model/
mv $BASE/domain/startinglineup/model/StartingLineupPK.java  $BASE/domain/foundation/baseball/lineup/model/
mv $BASE/domain/startinglineup/repository/StartingLineupRepository.java $BASE/domain/foundation/baseball/lineup/repository/
mv $BASE/domain/startinglineup/service/StartingLineupService.java       $BASE/domain/foundation/baseball/lineup/service/

echo "  ✅ lineup 파일 이동 완료 (12개)"

# --- Player.java (liveboard -> match/dto/MatchPlayerDto) ---
echo "[1-4-2] liveboard/Player.java -> match/dto/MatchPlayerDto.java"
if [ -f "$BASE/domain/liveboard/model/Player.java" ]; then
    mv $BASE/domain/liveboard/model/Player.java $BASE/domain/foundation/baseball/match/dto/MatchPlayerDto.java
    sed -i 's/class Player/class MatchPlayerDto/g' $BASE/domain/foundation/baseball/match/dto/MatchPlayerDto.java
    sed -i 's/package com\.sparta\.spartatigers\.domain\.liveboard\.model;/package com.sparta.spartatigers.domain.foundation.baseball.match.dto;/' $BASE/domain/foundation/baseball/match/dto/MatchPlayerDto.java
    
    # Update references in project
    find $BASE -name '*.java' -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.Player;/com.sparta.spartatigers.domain.foundation.baseball.match.dto.MatchPlayerDto;/g' {} +
    find $BASE -name '*.java' -exec sed -i 's/List<Player>/List<MatchPlayerDto>/g' {} +
    find $BASE -name '*.java' -exec sed -i 's/new Player/new MatchPlayerDto/g' {} +
    echo "  ✅ liveboard/Player.java -> MatchPlayerDto 이동 및 리네이밍 완료"
fi

# --- liveboard import 일괄 치환 (SED-3: LiveBoardMatchSubscriber 제외) ---
echo "[1-4-2] liveboard import 일괄 치환 (LiveBoardMatchSubscriber 제외)"

# match 관련
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.controller\.MatchController/com.sparta.spartatigers.domain.foundation.baseball.match.controller.MatchController/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.controller\.LiveBoardRoomController/com.sparta.spartatigers.domain.foundation.baseball.match.controller.LiveBoardRoomController/g' {} +

# liveboard 패키지 전체 치환 (match/lineup/team으로 분기)
# model 치환: 개별 처리 (Stadium, Player, Lineup* 는 이미 처리 또는 별도 목적지)
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.Match;/com.sparta.spartatigers.domain.foundation.baseball.match.model.Match;/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.MatchResult/com.sparta.spartatigers.domain.foundation.baseball.match.model.MatchResult/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.MatchScore/com.sparta.spartatigers.domain.foundation.baseball.match.model.MatchScore/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.InningTexts/com.sparta.spartatigers.domain.foundation.baseball.match.model.InningTexts/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.HomeAway/com.sparta.spartatigers.domain.foundation.baseball.match.model.HomeAway/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.LeagueType/com.sparta.spartatigers.domain.foundation.baseball.match.model.LeagueType/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.LiveBoard/com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoard/g' {} +

# lineup model
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.model\.LineupBatter/com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupBatter/g' {} +

# dto 치환
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.dto\.MatchScheduleResponseDto/com.sparta.spartatigers.domain.foundation.baseball.match.dto.MatchScheduleResponseDto/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.dto\.LiveBoard/com.sparta.spartatigers.domain.foundation.baseball.match.dto.LiveBoard/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.dto\.Lineup/com.sparta.spartatigers.domain.foundation.baseball.lineup.dto.Lineup/g' {} +

# repository 치환
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.repository\./com.sparta.spartatigers.domain.foundation.baseball.match.repository./g' {} +

# service 치환
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.service\.Lineup/com.sparta.spartatigers.domain.foundation.baseball.lineup.service.Lineup/g' {} +
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.service\./com.sparta.spartatigers.domain.foundation.baseball.match.service./g' {} +

# util 치환
find $BASE -name '*.java' \
    -not -path "*/liveboard/pubsub/LiveBoardMatchSubscriber.java" \
    -exec sed -i 's/com\.sparta\.spartatigers\.domain\.liveboard\.util\./com.sparta.spartatigers.domain.foundation.baseball.match.util./g' {} +

# 이동된 파일들의 package 선언 치환
find $BASE/domain/foundation/baseball/match -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.liveboard\./package com.sparta.spartatigers.domain.foundation.baseball.match./g' {} +
find $BASE/domain/foundation/baseball/lineup -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.liveboard\./package com.sparta.spartatigers.domain.foundation.baseball.lineup./g' {} +

echo "  ✅ liveboard import 치환 완료"

# ──────────────────────────────────────────────
# 1-4-2 추가: startinglineup import 치환
# ──────────────────────────────────────────────
echo "[1-4-2] startinglineup import 치환"
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.startinglineup\./com.sparta.spartatigers.domain.foundation.baseball.lineup./g' {} +

# 이동된 파일들의 package 선언 치환
find $BASE/domain/foundation/baseball/lineup -name '*.java' -exec sed -i \
    's/package com\.sparta\.spartatigers\.domain\.startinglineup\./package com.sparta.spartatigers.domain.foundation.baseball.lineup./g' {} +

echo "  ✅ startinglineup import 치환 완료"

# ──────────────────────────────────────────────
# 1-4-3. ranking → foundation/baseball/ranking/
# ──────────────────────────────────────────────
echo "[1-4-3] ranking → foundation/baseball/ranking/"
mkdir -p $BASE/domain/foundation/baseball/ranking/{controller,dto,repository,service}

mv $BASE/domain/ranking/controller/*.java  $BASE/domain/foundation/baseball/ranking/controller/
mv $BASE/domain/ranking/dto/*.java         $BASE/domain/foundation/baseball/ranking/dto/
mv $BASE/domain/ranking/repository/*.java  $BASE/domain/foundation/baseball/ranking/repository/
mv $BASE/domain/ranking/service/*.java     $BASE/domain/foundation/baseball/ranking/service/

find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.ranking\./com.sparta.spartatigers.domain.foundation.baseball.ranking./g' {} +

echo "  ✅ ranking 이동 완료"

# ──────────────────────────────────────────────
# 1-4-4. home + dashboard → foundation/baseball/home/
# ──────────────────────────────────────────────
echo "[1-4-4] home + dashboard → foundation/baseball/home/"
mkdir -p $BASE/domain/foundation/baseball/home/{controller,dto,service}

mv $BASE/domain/home/dto/*.java        $BASE/domain/foundation/baseball/home/dto/
mv $BASE/domain/home/service/*.java    $BASE/domain/foundation/baseball/home/service/
mv $BASE/domain/dashboard/controller/*.java $BASE/domain/foundation/baseball/home/controller/
mv $BASE/domain/dashboard/dto/*.java        $BASE/domain/foundation/baseball/home/dto/
mv $BASE/domain/dashboard/service/*.java    $BASE/domain/foundation/baseball/home/service/

find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.home\./com.sparta.spartatigers.domain.foundation.baseball.home./g' {} +
find $BASE -name '*.java' -exec sed -i \
    's/com\.sparta\.spartatigers\.domain\.dashboard\./com.sparta.spartatigers.domain.foundation.baseball.home./g' {} +

echo "  ✅ home + dashboard 이동 완료"

# ──────────────────────────────────────────────
# 1-5. 원본 디렉토리 삭제
# ──────────────────────────────────────────────
echo "[1-5] 원본 디렉토리 삭제"
rm -rf $BASE/domain/auth
rm -rf $BASE/domain/user
rm -rf $BASE/domain/favoriteteam
rm -rf $BASE/domain/team
rm -rf $BASE/domain/startinglineup
rm -rf $BASE/domain/ranking
rm -rf $BASE/domain/dashboard
rm -rf $BASE/domain/home

# liveboard: pubsub/LiveBoardMatchSubscriber만 남아있어야 함
echo "  📌 domain/liveboard/ 잔존 파일 확인:"
find $BASE/domain/liveboard -name '*.java' -type f 2>/dev/null || echo "  (없음)"

echo ""
echo "=========================================="
echo "Phase 1 완료"
echo "=========================================="
echo ""
echo "📌 수동 처리 필요:"
echo "  [C-2] foundation/baseball/match/dto/LiveBoardRoomResponseDto.java 날씨 필드 제거"
echo "  [C-2] foundation/baseball/match/service/LiveboardRoomService.java WeatherService 코드 제거"
echo ""
echo "수동 처리 완료 후 실행:"
echo "  ./scripts/verify.sh"
echo "  git add -A && git commit -m 'refactor: Phase 1 Foundation 계층 구축'"
echo "  git tag phase-1-done"
