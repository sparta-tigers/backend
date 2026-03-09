package com.sparta.spartatigers.domain.ranking.repository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.match.model.QMatch;
import com.sparta.spartatigers.domain.match.model.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.PostseasonStage;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;
import com.sparta.spartatigers.domain.team.model.QStadium;
import com.sparta.spartatigers.domain.team.model.QTeam;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeamRankingRepositoryCustomImpl implements TeamRankingRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	private final QMatch match = QMatch.match;
	private final QTeam homeTeam = new QTeam("homeTeam");
	private final QTeam awayTeam = new QTeam("awayTeam");
	private final QStadium stadium = QStadium.stadium;

	@Override
	public List<TeamRankingStat> applyTeamRecords(LocalDateTime anyday) {
		// 해당 날짜로부터 가장 최근에 진행된 경기의 season_year, leagueType을 찾는다
		Match lastestMatch = queryFactory
			.selectFrom(match)
			.where(
				match.matchTime.loe(anyday),
				match.leagueType.ne(LeagueType.POST_SEASON)
				)
			.orderBy(match.matchTime.desc())
			.fetchFirst();

		if (lastestMatch == null) {
			throw new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND);
		}

		// 파악된 시즌 연도와 리그 종류를 기준으로 집계 범위 확정
		LeagueType leagueType = lastestMatch.getLeagueType();
		int currentSeason = lastestMatch.getSeasonYear();
		LocalDateTime to = anyday.toLocalDate().atTime(23,59,59);

		// 홈+원정 데이터 각각 집계
		List<TeamRankingStat> homeAgg = aggregateHome(leagueType, currentSeason, to);
		List<TeamRankingStat> awayAgg = aggregateAway(leagueType, currentSeason, to);

		// 팀별로 데이터 병합
		Map<Long, TeamRankingStat> merged = new HashMap<>();
		mergeInto(merged, homeAgg);
		mergeInto(merged, awayAgg);

		return merged.values().stream().toList();
	}

	public List<TeamRankingStat> applyTeamRecordsByYear(int year, LeagueType leagueType) {

		LocalDateTime endOfyear = Year.of(year).atMonth(12).atEndOfMonth().atTime(23,59,59);

		// 홈+원정 데이터 각각 집계
		List<TeamRankingStat> homeAgg = aggregateHome(leagueType, year, endOfyear);
		List<TeamRankingStat> awayAgg = aggregateAway(leagueType, year, endOfyear);

		// 팀별로 데이터 병합
		Map<Long, TeamRankingStat> merged = new HashMap<>();
		mergeInto(merged, homeAgg);
		mergeInto(merged, awayAgg);

		return merged.values().stream().toList();
	}

	public List<Match> findAllPostSeasonMatches(int year) {
		return queryFactory
			.selectFrom(match)
			.join(match.homeTeam, homeTeam).fetchJoin()
			.join(match.awayTeam, awayTeam).fetchJoin()
			.leftJoin(match.stadium, stadium).fetchJoin()
			.where(
				match.leagueType.eq(LeagueType.POST_SEASON),
				match.seasonYear.eq(year)
			)
			.orderBy(match.matchTime.asc())
			.fetch();
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
					stat.getLeagueType(),
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
	private List<TeamRankingStat> aggregateHome(LeagueType leagueType, int seasonYear, LocalDateTime anyday) {
		return queryFactory
			.select(Projections.constructor(
				TeamRankingStat.class,
				match.leagueType,
				homeTeam.id,
				homeTeam.name,
				new CaseBuilder().when(match.matchResult.eq(MatchResult.HOME_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.AWAY_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.DRAW)).then(1).otherwise(0).sum()
			))
			.from(match)
			.join(match.homeTeam, homeTeam)
			.where(
				match.leagueType.eq(leagueType),
				match.seasonYear.eq(seasonYear),
				match.matchTime.loe(anyday),
				match.matchResult.in(MatchResult.HOME_WIN, MatchResult.AWAY_WIN, MatchResult.DRAW)
			)
			.groupBy(homeTeam.id, homeTeam.name)
			.fetch();
	}

	// 특정 기간의 경기중 원정팀에 대해 승/패/무 횟수를 집계합니다.
	private List<TeamRankingStat> aggregateAway(LeagueType leagueType, int seasonYear, LocalDateTime anyday) {
		return queryFactory
			.select(Projections.constructor(
				TeamRankingStat.class,
				match.leagueType,
				awayTeam.id,
				awayTeam.name,
				new CaseBuilder().when(match.matchResult.eq(MatchResult.AWAY_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.HOME_WIN)).then(1).otherwise(0).sum(),
				new CaseBuilder().when(match.matchResult.eq(MatchResult.DRAW)).then(1).otherwise(0).sum()
			))
			.from(match)
			.join(match.awayTeam, awayTeam)
			.where(
				match.leagueType.eq(leagueType),
				match.seasonYear.eq(seasonYear),
				match.matchTime.loe(anyday),
				match.matchResult.in(MatchResult.HOME_WIN, MatchResult.AWAY_WIN, MatchResult.DRAW)
			)
			.groupBy(awayTeam.id, awayTeam.name)
			.fetch();
	}
}
