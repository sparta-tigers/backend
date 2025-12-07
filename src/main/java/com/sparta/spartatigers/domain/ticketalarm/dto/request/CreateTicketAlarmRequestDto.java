package com.sparta.spartatigers.domain.ticketalarm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTicketAlarmRequestDto {

	Long matchId;
	Long teamId;
	Integer preAlarmTime;
	String membership;

}
