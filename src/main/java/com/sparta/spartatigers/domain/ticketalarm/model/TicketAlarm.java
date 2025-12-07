package com.sparta.spartatigers.domain.ticketalarm.model;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_alarms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAlarm extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "ticket_alarm_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "match_id", nullable = false)
	private Match match;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name ="booking_policy_id", nullable = false)
	private TeamBookingPolicy teamBookingPolicy;

	@Column(nullable = false)
	private Integer minusBefore;

	@Column(nullable = false)
	private LocalDateTime alarmTime;

	public TicketAlarm(
		User user,
		Match match,
		TeamBookingPolicy policy,
		Integer minusBefore,
		LocalDateTime alarmTime
	) {
		this.user = user;
		this.match = match;
		this.teamBookingPolicy = policy;
		this.minusBefore = minusBefore;
		this.alarmTime = alarmTime;
	}

	public static TicketAlarm of (User user, Match match, TeamBookingPolicy policy, Integer minusBefore, LocalDateTime alarmTime) {
		return new TicketAlarm(
			user, 
			match, 
			policy, 
			minusBefore, 
			alarmTime);
	}
}
