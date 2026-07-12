package com.sparta.spartatigers.domain.foundation.baseball.match.dto;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.InningTexts;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardData;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.LiveBoardStatus;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.Match;
import com.sparta.spartatigers.domain.foundation.baseball.match.model.MatchResult;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.TeamCode;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LiveBoardRoomResponseDto {

    private final String roomId; // null일수 있음
    private final Long matchId;
    private final String title;
    private final LocalDateTime matchTime;

    // private double temperature; //기온(TMP) - 초단기실황, 초단기예보
    // private SkyStatus skyStatus; // 하늘상태(SKY) - 초단기예보, 단기예보

    private final LiveBoardStatus liveBoardStatus;

    private final String awayTeamName;
    private final TeamCode awayTeamCode; // 이미지
    private final String homeTeamName;
    private final TeamCode homeTeamCode; // 이미지
    private final MatchResult matchResult;
    private final String stadium;
    private final Long connectCount;
    // @JsonProperty("isTodayMatch")
    // private boolean isTodayMatch; // TODO : 삭제

    private final InningTexts inningTexts;
    private final LiveBoardDataResponseDto liveBoardData;

    public static LiveBoardRoomResponseDto fromUpcomingMatch(Match match) {
        return LiveBoardRoomResponseDto.builder()
            .roomId("LIVEBOARD_" + match.getId())
            .matchId(match.getId())
            .title(
                match.getAwayTeam().getName() +
                    "VS" +
                    match.getHomeTeam().getName()
            )
            .matchTime(match.getMatchTime())
            .liveBoardStatus(LiveBoardStatus.UPCOMING)
            .awayTeamName(match.getAwayTeam().getName())
            .awayTeamCode(match.getAwayTeam().getCode())
            .homeTeamName(match.getHomeTeam().getName())
            .homeTeamCode(match.getHomeTeam().getCode())
            .matchResult(match.getMatchResult()) // 없을수있음
            .stadium(
                match.getStadium() != null ? match.getStadium().getName() : null
            )
            .connectCount(0L)
            .build();
    }

    public static LiveBoardRoomResponseDto fromTodayMatch(
        Match match,
        LiveBoardRoom room,
        long connectCount,
        InningTexts inningTexts,
        LiveBoardData liveBoardData
    ) {
        return LiveBoardRoomResponseDto.builder()
            .roomId(room.getRoomId())
            .matchId(match.getId())
            .title(room.getTitle())
            .matchTime(match.getMatchTime())
            .liveBoardStatus(LiveBoardStatus.TODAY)
            .awayTeamName(match.getAwayTeam().getName())
            .awayTeamCode(match.getAwayTeam().getCode())
            .homeTeamName(match.getHomeTeam().getName())
            .homeTeamCode(match.getHomeTeam().getCode())
            .matchResult(match.getMatchResult()) // 없을수있음
            .stadium(
                match.getStadium() != null ? match.getStadium().getName() : null
            )
            .connectCount(connectCount)
            .inningTexts(inningTexts)
            .liveBoardData(LiveBoardDataResponseDto.from(liveBoardData))
            .build();
    }

    public static LiveBoardRoomResponseDto fromPastMatch(
        Match match,
        LiveBoardRoom room
    ) {
        return LiveBoardRoomResponseDto.builder()
            .roomId(room.getRoomId())
            .matchId(match.getId())
            .title(room.getTitle())
            .matchTime(match.getMatchTime())
            .liveBoardStatus(LiveBoardStatus.PAST)
            .awayTeamName(match.getAwayTeam().getName())
            .awayTeamCode(match.getAwayTeam().getCode())
            .homeTeamName(match.getHomeTeam().getName())
            .homeTeamCode(match.getHomeTeam().getCode())
            .matchResult(match.getMatchResult()) // 없을수없음
            .stadium(
                match.getStadium() != null ? match.getStadium().getName() : null
            )
            .connectCount(0L)
            .build();
    }
}
