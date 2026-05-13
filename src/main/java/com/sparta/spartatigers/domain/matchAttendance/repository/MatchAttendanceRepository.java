package com.sparta.spartatigers.domain.liveboard.matchAttendance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.spartatigers.domain.liveboard.matchAttendance.model.MatchAttendance;

public interface MatchAttendanceRepository extends JpaRepository<MatchAttendance, Long> {

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.stadium"})
    Page<MatchAttendance> findAllByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.stadium"})
    java.util.Optional<MatchAttendance> findByUser_IdAndMatch_Id(Long userId, Long matchId);

    long countByUser_IdAndMatch_SeasonYear(Long userId, int seasonYear);
}
