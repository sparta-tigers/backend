package com.sparta.spartatigers.domain.foundation.user.favoriteteam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavTeamRequestDto {

	private Long teamId;
	private String teamCode;
}
