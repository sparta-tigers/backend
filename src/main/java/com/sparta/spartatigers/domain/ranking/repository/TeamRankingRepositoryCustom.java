package com.sparta.spartatigers.domain.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.ranking.dto.LeagueType;
import com.sparta.spartatigers.domain.ranking.dto.MatchDetailDto;
import com.sparta.spartatigers.domain.ranking.dto.PostseasonStage;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;

public interface TeamRankingRepositoryCustom {

	List<TeamRankingStat> applyTeamRecords (LocalDateTime anyday);
	List<TeamRankingStat> applyTeamRecordsByYear (int year, LeagueType leagueType);
	Map<PostseasonStage, List<Match>> classifyStages (int year);
}
