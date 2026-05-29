package com.sparta.spartatigers.domain.foundation.baseball.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.Match;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LeagueType;
import com.sparta.spartatigers.domain.foundation.baseball.ranking.dto.TeamRankingStat;

public interface TeamRankingRepositoryCustom {

	List<TeamRankingStat> applyTeamRecords(LocalDateTime anyday, LeagueType leagueType);
	List<TeamRankingStat> applyTeamRecordsByYear (int year, LeagueType leagueType);
	List<Match> findAllPostSeasonMatches(int year);
}
