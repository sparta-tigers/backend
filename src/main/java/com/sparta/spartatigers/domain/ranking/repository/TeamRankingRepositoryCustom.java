package com.sparta.spartatigers.domain.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.liveboard.match.model.Match;
import com.sparta.spartatigers.domain.liveboard.match.model.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;

public interface TeamRankingRepositoryCustom {

	List<TeamRankingStat> applyTeamRecords(LocalDateTime anyday, LeagueType leagueType);
	List<TeamRankingStat> applyTeamRecordsByYear (int year, LeagueType leagueType);
	List<Match> findAllPostSeasonMatches(int year);
}
