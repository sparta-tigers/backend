package com.sparta.spartatigers.domain.startinglineup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.liveboard.dto.LineupCacheDto;
import com.sparta.spartatigers.domain.liveboard.match.model.Match;
import com.sparta.spartatigers.domain.liveboard.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.startinglineup.model.LineupPlayer;
import com.sparta.spartatigers.domain.startinglineup.model.StartingLineup;
import com.sparta.spartatigers.domain.startinglineup.model.StartingLineupPK;
import com.sparta.spartatigers.domain.startinglineup.repository.StartingLineupRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartingLineupService {

    private final StartingLineupRepository startingLineupRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public void saveLineupFromCache(LineupCacheDto cacheDto) {
        if (cacheDto == null || cacheDto.getMatchId() == null) return;

        Match match = matchRepository.findById(cacheDto.getMatchId())
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));

        // 홈팀 라인업 저장
        if (cacheDto.getHomeBatters() != null && !cacheDto.getHomeBatters().isEmpty()) {
            saveTeamLineup(match, match.getHomeTeam().getId(), cacheDto.getHomeBatters());
        }

        // 어웨이팀 라인업 저장
        if (cacheDto.getAwayBatters() != null && !cacheDto.getAwayBatters().isEmpty()) {
            saveTeamLineup(match, match.getAwayTeam().getId(), cacheDto.getAwayBatters());
        }
    }

    private void saveTeamLineup(Match match, Long teamId, List<com.sparta.spartatigers.domain.liveboard.model.LineupBatter> batters) {
        StartingLineupPK pk = new StartingLineupPK(match.getId(), teamId);
        
        // 이미 존재하면 건너뜀 (또는 업데이트 로직 추가 가능)
        if (startingLineupRepository.existsById(pk)) {
            return;
        }

        StartingLineup lineup = StartingLineup.create(match, match.getHomeTeam().getId().equals(teamId) ? match.getHomeTeam() : match.getAwayTeam());
        
        List<LineupPlayer> players = batters.stream()
                .map(b -> LineupPlayer.of(Integer.parseInt(b.getBattingOrder()), b.getPosition(), b.getName()))
                .collect(Collectors.toList());
        
        lineup.addPlayers(players);
        startingLineupRepository.save(lineup);
        log.info("Saved starting lineup to DB for matchId: {}, teamId: {}", match.getId(), teamId);
    }
}
