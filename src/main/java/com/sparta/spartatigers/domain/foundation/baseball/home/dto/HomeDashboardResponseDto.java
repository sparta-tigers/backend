package com.sparta.spartatigers.domain.foundation.baseball.home.dto;

import java.util.List;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupBatter;
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
    private List<LineupBatter> todayLineup;
}
