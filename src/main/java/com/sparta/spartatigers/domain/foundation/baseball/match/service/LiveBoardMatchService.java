package com.sparta.spartatigers.domain.foundation.baseball.match.service;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardData;
import com.sparta.spartatigers.domain.foundation.baseball.match.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveBoardMatchService {

    private final MatchRepository matchRepository;

    /**
     * 경기 점수 및 결과 정보를 DB에 동기화함.
     * 프록시 기반 트랜잭션 보장을 위해 별도 서비스로 분리.
     */
    @Transactional
    public void updateMatchScore(LiveBoardData data) {
        if (
            data == null ||
            data.getMatchId() == null ||
            data.getMatchScore() == null
        ) {
            return;
        }

        matchRepository
            .findById(data.getMatchId())
            .ifPresentOrElse(
                match -> {
                    try {
                        int homeScore = Integer.parseInt(
                            data.getMatchScore().getHomeScore()
                        );
                        int awayScore = Integer.parseInt(
                            data.getMatchScore().getAwayScore()
                        );

                        // 엔티티 내부 메서드를 활용한 상태 변경 (Zero Magic)
                        match.updateScore(homeScore, awayScore);
                    } catch (NumberFormatException e) {
                        log.warn(
                            "Invalid score format for matchId: {}. Home: {}, Away: {}",
                            data.getMatchId(),
                            data.getMatchScore().getHomeScore(),
                            data.getMatchScore().getAwayScore()
                        );
                    }
                },
                () ->
                    log.warn(
                        "Match not found while synchronizing score. matchId={}",
                        data.getMatchId()
                    )
            );
    }
}
