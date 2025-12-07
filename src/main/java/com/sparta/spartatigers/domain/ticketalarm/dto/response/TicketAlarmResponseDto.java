package com.sparta.spartatigers.domain.ticketalarm.dto.response;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.ticketalarm.model.TeamBookingPolicy;
import com.sparta.spartatigers.domain.ticketalarm.model.TicketAlarm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketAlarmResponseDto {

	// Alarm 정보
	private Long alarmId;
	private Integer minusBefore;
	private LocalDateTime alarmTime;

	// Match 정보
	private Long matchId;
	private String homeTeam;
	private String awayTeam;
	private LocalDateTime matchTime;
	private String stadiumName;

	// Booking Policy 정보
	private Long bookingPolicyId;
	private String membership;
	private String ticketUrl;
	private LocalDateTime openBookingTime;

	public static TicketAlarmResponseDto from(TicketAlarm alarm, LocalDateTime openBookingTime) {
		Match match = alarm.getMatch();
		TeamBookingPolicy policy = alarm.getTeamBookingPolicy();

		return new TicketAlarmResponseDto(
			alarm.getId(),
			alarm.getMinusBefore(),
			alarm.getAlarmTime(),

			match.getId(),
			match.getHomeTeam().getName(),
			match.getAwayTeam().getName(),
			match.getMatchTime(),
			match.getStadium().getName(),

			policy.getId(),
			policy.getMembership(),
			policy.getTicketUrl(),
			openBookingTime
		);
	}

}
