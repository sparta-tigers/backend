package com.sparta.spartatigers.domain.matchAttendance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.spartatigers.domain.matchAttendance.model.MatchAttendance;

public interface MatchAttendanceRepository extends JpaRepository<MatchAttendance, Long> {

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.stadium"})
    Page<MatchAttendance> findAllByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam", "match.stadium"})
    java.util.Optional<MatchAttendance> findByUser_IdAndMatch_Id(Long userId, Long matchId);

    @Query("SELECT COUNT(ma) FROM MatchAttendance ma WHERE ma.user.id = :userId AND ma.match.seasonYear = :seasonYear")
    long countByUser_IdAndMatch_SeasonYear(@Param("userId") Long userId, @Param("seasonYear") int seasonYear);
}
