package com.sparta.spartatigers.domain.startinglineup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.startinglineup.model.StartingLineup;
import com.sparta.spartatigers.domain.startinglineup.model.StartingLineupPK;

public interface StartingLineupRepository extends JpaRepository<StartingLineup, StartingLineupPK> {
	List<StartingLineup> findByMatchId(Long matchId);
}
