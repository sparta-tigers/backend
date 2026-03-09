package com.sparta.spartatigers.domain.team.repository;

import java.util.Optional;

import com.sparta.spartatigers.domain.team.model.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {

	Optional<Stadium> findById(Long id);

}
