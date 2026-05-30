package com.sparta.spartatigers.domain.core.ticketalarm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.core.ticketalarm.model.TicketAlarm;

public interface TicketAlarmRepository extends JpaRepository<TicketAlarm, Long> {

	@Query("""
			SELECT t
			FROM TicketAlarm t
			JOIN FETCH t.match tm
			JOIN FETCH t.teamBookingPolicy tbp
			JOIN FETCH tm.homeTeam ht
			JOIN FETCH tm.awayTeam at
			JOIN FETCH tm.stadium st
			JOIN FETCH t.user u
			WHERE t.user.id = :userId
			ORDER BY t.alarmTime ASC
			""")
	List<TicketAlarm> findAllByUserId(Long userId);

	@EntityGraph(attributePaths = {
			"match",
			"match.homeTeam",
			"match.awayTeam",
			"match.stadium",
			"teamBookingPolicy"
	})
	Page<TicketAlarm> findByUserId(Long userId, Pageable pageable);

	Optional<TicketAlarm> findById(Long alarmId);

	long countByUserId(Long userId);

}
