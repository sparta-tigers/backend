package com.sparta.spartatigers.domain.ticketalarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.ticketalarm.model.TicketAlarm;

public interface TicketAlarmRepository extends JpaRepository<TicketAlarm, Long> {
}
