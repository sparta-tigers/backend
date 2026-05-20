package com.sparta.spartatigers.domain.liveboard.model;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchTest {

    @Test
    @DisplayName("matchTime이 null일 때 updateScore를 시도해도 NullPointerException 없이 성공한다")
    void updateScore_withNullMatchTime_noNpe() {
        // given
        Match match = Match.builder()
                .matchTime(null)
                .matchResult(MatchResult.NOT_PLAYED)
                .build();

        // when
        match.updateScore(3, 2);

        // then
        assertThat(match.getHomeScore()).isEqualTo(3);
        assertThat(match.getAwayScore()).isEqualTo(2);
        // matchTime이 null이므로 PROCEEDING으로 상태 변환이 되지 않고 NOT_PLAYED로 유지
        assertThat(match.getMatchResult()).isEqualTo(MatchResult.NOT_PLAYED);
    }

    @Test
    @DisplayName("경기 시작 예정 시간 이전(미래)에 스코어를 업데이트해도 진행중(PROCEEDING) 상태로 변환되지 않는다")
    void updateScore_beforeMatchTime_staysNotPlayed() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        Match match = Match.builder()
                .matchTime(futureTime)
                .matchResult(MatchResult.NOT_PLAYED)
                .build();

        // when
        match.updateScore(1, 0);

        // then
        assertThat(match.getHomeScore()).isEqualTo(1);
        assertThat(match.getAwayScore()).isEqualTo(0);
        assertThat(match.getMatchResult()).isEqualTo(MatchResult.NOT_PLAYED);
    }

    @Test
    @DisplayName("경기 시작 예정 시간 지났을 때 스코어를 업데이트하면 진행중(PROCEEDING) 상태로 변환된다")
    void updateScore_afterMatchTime_changesToProceeding() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);
        Match match = Match.builder()
                .matchTime(pastTime)
                .matchResult(MatchResult.NOT_PLAYED)
                .build();

        // when
        match.updateScore(2, 4);

        // then
        assertThat(match.getHomeScore()).isEqualTo(2);
        assertThat(match.getAwayScore()).isEqualTo(4);
        assertThat(match.getMatchResult()).isEqualTo(MatchResult.PROCEEDING);
    }

    @Test
    @DisplayName("경기 시작 예정 시간 이전에 CANCEL 또는 NOT_PLAYED 이외의 결과를 주입하려고 하면 예외가 발생한다")
    void validateMatchTimeState_beforeMatchTime_throwsExceptionForInvalidResult() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        Match match = Match.builder()
                .matchTime(futureTime)
                .matchResult(MatchResult.NOT_PLAYED)
                .build();

        // when & then
        assertThatThrownBy(() -> match.updateFinalResult(MatchResult.PROCEEDING, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("경기 예정 시간 이전에는 CANCEL 또는 NOT_PLAYED 상태만 가질 수 있습니다.");
    }

    @Test
    @DisplayName("경기 시작 예정 시간 이전에 CANCEL 또는 NOT_PLAYED로 결과 업데이트 시 정상 처리된다")
    void validateMatchTimeState_beforeMatchTime_allowsCancelAndNotPlayed() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        Match match = Match.builder()
                .matchTime(futureTime)
                .matchResult(MatchResult.NOT_PLAYED)
                .build();

        // when
        match.updateFinalResult(MatchResult.CANCEL, 0, 0);

        // then
        assertThat(match.getMatchResult()).isEqualTo(MatchResult.CANCEL);
    }
}
