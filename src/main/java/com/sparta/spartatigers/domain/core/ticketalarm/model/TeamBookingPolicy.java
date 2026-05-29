package com.sparta.spartatigers.domain.core.ticketalarm.model;

import com.sparta.spartatigers.domain.foundation.baseball.team.model.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_booking_policy")
@Getter
@NoArgsConstructor
public class TeamBookingPolicy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "booking_policy_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(nullable = false)
	private String membership;

	@Column(nullable = false)
	private Integer openDaysBefore;

	@Column(nullable = false)
	private Integer openHourOfDay;

	@Column(nullable = false)
	private Integer openMinOfDay;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplyScope applyScope;

	@Column(nullable = false)
	private Integer seriesCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BaseType baseType;

	@Column(nullable = false)
	private Integer seasonYear;

	@Column(nullable = false)
	private boolean active;

	@Column(nullable = false)
	private String ticketUrl;

}
