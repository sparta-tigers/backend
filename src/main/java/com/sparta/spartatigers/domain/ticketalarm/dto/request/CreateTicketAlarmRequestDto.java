package com.sparta.spartatigers.domain.ticketalarm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketAlarmRequestDto {

	Long matchId;
	Long teamId;
	Integer preAlarmTime;
	String membership;

}
