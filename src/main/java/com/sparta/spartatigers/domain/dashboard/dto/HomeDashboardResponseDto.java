package com.sparta.spartatigers.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDashboardResponseDto {
    private String nickname;
    private Long enrollmentDays;
    private Long remainingMatches;
    private String favoriteTeamCode;
}
