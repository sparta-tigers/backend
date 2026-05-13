package com.sparta.spartatigers.domain.liveboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sparta.spartatigers.domain.liveboard.model.Stadium;

/**
 * 야구장 정보 Repository
 * 
 * Why: Stadium 엔티티를 liveboard 도메인의 표준 저장소 계층으로 관리함.
 */
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

}
