package com.sparta.spartatigers.domain.ranking.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;
import com.sparta.spartatigers.domain.team.model.Team;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamRankingService {

	private final MatchRepository matchRepository;

	public List<TeamRankingResponseDto> getRankings() {

		// 1. 종료된 경기 조회
		List<Match> matches = matchRepository.findFinishedMatches();

		Map<Long, TeamRankingStat> statMap = new HashMap<>();
		for( Match match : matches) {
			applyMatchResult(statMap, match);
		}

		List<TeamRankingStat> sortedStats = statMap.values().stream().sorted(rankingComparator()).toList();

		List<TeamRankingResponseDto> response = new ArrayList<>();
		int rank = 1;

		for (TeamRankingStat stat : sortedStats) {
			response.add(
				TeamRankingResponseDto.of(
					rank++,
					stat.getTeamId(),
					stat.getTeamName(),
					stat.getMatchCount(),
					stat.getWinCount(),
					stat.getLoseCount(),
					stat.getDrawCount(),
					stat.getWinRate())
			);
		}
		return response;
	}

	private Comparator<TeamRankingStat> rankingComparator() {
		return Comparator
			.comparing(TeamRankingStat::getWinRate, Comparator.reverseOrder())
			.thenComparing(TeamRankingStat::getWinCount, Comparator.reverseOrder())
			.thenComparing(TeamRankingStat::getLoseCount);
	}

	private void applyMatchResult( Map<Long, TeamRankingStat> statMap, Match match) {

		if (match.getMatchResult() == MatchResult.NOT_PLAYED) {
			return;
		}

		Team home = match.getHomeTeam();
		Team away = match.getAwayTeam();

		TeamRankingStat homeStat =
			statMap.computeIfAbsent(home.getId(), id -> TeamRankingStat.from(id, home.getName()));

		TeamRankingStat awayStat =
			statMap.computeIfAbsent(away.getId(), id -> TeamRankingStat.from(id, away.getName()));

		switch (match.getMatchResult()) {
			case HOME_WIN -> {
				homeStat.recordWin();
				awayStat.recordLose();
			}
			case AWAY_WIN -> {
				awayStat.recordWin();
				homeStat.recordLose();
			}
			case DRAW -> {
				homeStat.recordDraw();
				awayStat.recordDraw();
			}
		}



	}
}
