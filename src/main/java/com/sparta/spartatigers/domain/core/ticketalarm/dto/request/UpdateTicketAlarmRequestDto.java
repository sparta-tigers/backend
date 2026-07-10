package com.sparta.spartatigers.domain.core.ticketalarm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketAlarmRequestDto {

	@Min(1)
	@Max(180)
	Integer preAlarmTime;

	String membership;

}
