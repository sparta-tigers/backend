package com.sparta.spartatigers.domain.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.spartatigers.domain.ranking.dto.TeamRankingStat;

public interface TeamRankingRepositoryCustom {

	List<TeamRankingStat> applyTeamRecords (LocalDateTime from, LocalDateTime to);
}
