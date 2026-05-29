package com.sparta.spartatigers.domain.core.ticketalarm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketAlarmRequestDto {

	@NotNull
	private Long matchId;

	@NotNull
	private Long teamId;

	@NotNull
	@Min(1)
	@Max(180)
	private Integer preAlarmTime;

	private String membership;

}
