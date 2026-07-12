package com.sparta.spartatigers.domain.foundation.baseball.lineup.service;

import com.sparta.spartatigers.domain.foundation.baseball.lineup.dto.LineupCacheDto;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupPlayer;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.StartingLineup;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.StartingLineupPK;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.repository.StartingLineupRepository;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.Match;
import com.sparta.spartatigers.domain.foundation.baseball.match.repository.MatchRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartingLineupService {

    private final StartingLineupRepository startingLineupRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public void saveLineupFromCache(LineupCacheDto cacheDto) {
        if (cacheDto == null || cacheDto.getMatchId() == null) return;

        Match match = matchRepository
            .findById(cacheDto.getMatchId())
            .orElseThrow(() ->
                new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND)
            );

        // 홈팀 라인업 저장
        if (
            cacheDto.getHomeBatters() != null &&
            !cacheDto.getHomeBatters().isEmpty()
        ) {
            saveTeamLineup(
                match,
                match.getHomeTeam().getId(),
                cacheDto.getHomeBatters()
            );
        }

        // 어웨이팀 라인업 저장
        if (
            cacheDto.getAwayBatters() != null &&
            !cacheDto.getAwayBatters().isEmpty()
        ) {
            saveTeamLineup(
                match,
                match.getAwayTeam().getId(),
                cacheDto.getAwayBatters()
            );
        }
    }

    private void saveTeamLineup(
        Match match,
        Long teamId,
        List<
            com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupBatter
        > batters
    ) {
        StartingLineupPK pk = new StartingLineupPK(match.getId(), teamId);

        // 이미 존재하면 건너뜀
        if (startingLineupRepository.existsById(pk)) {
            return;
        }

        com.sparta.spartatigers.domain.foundation.baseball.team.model.Team targetTeam;
        if (match.getHomeTeam().getId().equals(teamId)) {
            targetTeam = match.getHomeTeam();
        } else if (match.getAwayTeam().getId().equals(teamId)) {
            targetTeam = match.getAwayTeam();
        } else {
            log.warn(
                "[LINEUP] Team ID {} is not associated with Match ID {} (neither Home nor Away)",
                teamId,
                match.getId()
            );
            return;
        }

        StartingLineup lineup = StartingLineup.create(match, targetTeam);

        List<LineupPlayer> players = batters
            .stream()
            .map(b -> {
                try {
                    int order = Integer.parseInt(b.getBattingOrder());
                    return LineupPlayer.of(order, b.getPosition(), b.getName());
                } catch (NumberFormatException e) {
                    log.warn(
                        "[LINEUP] Invalid batting order '{}' for player '{}' in Match {}",
                        b.getBattingOrder(),
                        b.getName(),
                        match.getId()
                    );
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        lineup.addPlayers(players);
        startingLineupRepository.save(lineup);
        log.info(
            "Saved starting lineup to DB for matchId: {}, teamId: {}",
            match.getId(),
            teamId
        );
    }
}
