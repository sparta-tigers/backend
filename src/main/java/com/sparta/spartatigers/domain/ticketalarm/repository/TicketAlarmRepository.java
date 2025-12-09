package com.sparta.spartatigers.domain.ticketalarm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.ticketalarm.model.TicketAlarm;

public interface TicketAlarmRepository extends JpaRepository<TicketAlarm, Long> {

	@Query(
		"""
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
		"""
	)
	List<TicketAlarm> findAllByUserId(Long userId);

	Optional<TicketAlarm> findById(Long alarmId);

}
