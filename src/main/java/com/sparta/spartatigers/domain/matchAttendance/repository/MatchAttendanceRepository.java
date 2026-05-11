package com.sparta.spartatigers.domain.liveboard.matchAttendance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.liveboard.matchAttendance.model.MatchAttendance;

public interface MatchAttendanceRepository extends JpaRepository<MatchAttendance, Long> {

	Page<MatchAttendance> findAllByUser_Id(Long userId, Pageable pageable);
}
