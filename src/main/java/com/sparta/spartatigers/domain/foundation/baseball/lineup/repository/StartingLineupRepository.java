package com.sparta.spartatigers.domain.foundation.baseball.lineup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.StartingLineup;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.StartingLineupPK;

public interface StartingLineupRepository extends JpaRepository<StartingLineup, StartingLineupPK> {
	List<StartingLineup> findByMatchId(Long matchId);
}
