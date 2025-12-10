package com.sparta.spartatigers.domain.ticketalarm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.ticketalarm.model.TeamBookingPolicy;

public interface TeamBookingPolicyRepository extends JpaRepository<TeamBookingPolicy, Long> {

	List<TeamBookingPolicy> findAllByTeamId(Long teamId);

	TeamBookingPolicy findByTeamIdAndMembership(Long teamId, String membership);

	@Query(
		"""
		SELECT p
		FROM TeamBookingPolicy p
		WHERE p.team.id = :teamId
			AND p.membership = '일반'
		"""
	)
	TeamBookingPolicy findDefaultPolicyByTeamId (Long teamId);
}
