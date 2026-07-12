package com.sparta.spartatigers.domain.foundation.baseball.team.repository;

import com.sparta.spartatigers.domain.foundation.baseball.team.model.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 야구장 정보 Repository
 *
 * Why: Stadium 엔티티를 liveboard 도메인의 표준 저장소 계층으로 관리함.
 */
public interface StadiumRepository extends JpaRepository<Stadium, Long> {}
