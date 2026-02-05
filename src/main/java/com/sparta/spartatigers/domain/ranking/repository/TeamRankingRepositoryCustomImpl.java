package com.sparta.spartatigers.domain.ranking.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.match.model.QMatch;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;
import com.sparta.spartatigers.domain.team.model.QTeam;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamRankingRepositoryCustomImpl implements TeamRankingRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	private final QMatch match = QMatch.match;
	private final QTeam homeTeam = QTeam.team;
	private final QTeam awayTeam = QTeam.team;

	@Override
	public List<TeamRankingStat> applyTeamRecords(LocalDateTime from, LocalDateTime to) {

		List<TeamRankingStat> homeAgg = aggregateHome(from, to);
		List<TeamRankingStat> awayAgg = aggregateAway(from, to);

		Map<Long, TeamRankingStat> merged = new HashMap<>();

		mergeInto(merged, homeAgg);
		mergeInto(merged, awayAgg);

		return merged.values().stream().toList();
	}

	// 집계결과를 팀별 누적 집계에 합산합니다.
	private void mergeInto(Map<Long, TeamRankingStat> rankingStatMap, List<TeamRankingStat> rowStats) {
		for ( TeamRankingStat stat : rowStats) {
			TeamRankingStat alreadyApplied = rankingStatMap.get(stat.getTeamId());
			if(alreadyApplied == null) {
				rankingStatMap.put(stat.getTeamId(), stat);
				continue;
			}

			rankingStatMap.put(stat.getTeamId(),
				new TeamRankingStat(
					stat.getTeamId(),
					stat.getTeamName(),
					alreadyApplied.getWinCount() + stat.getWinCount(),
					alreadyApplied.getLoseCount() + stat.getLoseCount(),
					alreadyApplied.getDrawCount() + stat.getDrawCount()
				)
			);
		}
	}

	// 특정 기간의 경기중 홈팀에 대해 승/패/무 횟수를 집계합니다.
	private List<TeamRankingStat> aggregateHome(LocalDateTime from, LocalDateTime to) {
		return queryFactory
			.select(Projections.constructor(
				TeamRankingStat.class,
				homeTeam.id,
				homeTeam.name,
				new CaseBuilder().when(match.matchResult.eq(MatchResult.HOME_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.AWAY_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.DRAW)).then(1).otherwise(0).sum()
			))
			.from(match)
			.join(match.homeTeam, homeTeam)
			.where(
				match.matchTime.between(from, to),
				match.matchResult.in(MatchResult.HOME_WIN, MatchResult.AWAY_WIN, MatchResult.DRAW)
			)
			.groupBy(homeTeam.id, homeTeam.name)
			.fetch();
	}

	// 특정 기간의 경기중 원정팀에 대해 승/패/무 횟수를 집계합니다.
	private List<TeamRankingStat> aggregateAway(LocalDateTime from, LocalDateTime to) {
		return queryFactory
			.select(Projections.constructor(
				TeamRankingStat.class,
				awayTeam.id,
				awayTeam.name,
				new CaseBuilder().when(match.matchResult.eq(MatchResult.AWAY_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.HOME_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.DRAW)).then(1).otherwise(0).sum()
			))
			.from(match)
			.join(match.awayTeam, awayTeam)
			.where(
				match.matchTime.between(from, to),
				match.matchResult.in(MatchResult.HOME_WIN, MatchResult.AWAY_WIN, MatchResult.DRAW)
			)
			.groupBy(awayTeam.id, awayTeam.name)
			.fetch();
	}
}
