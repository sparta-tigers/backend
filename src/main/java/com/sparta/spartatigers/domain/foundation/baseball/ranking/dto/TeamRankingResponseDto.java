package com.sparta.spartatigers.domain.foundation.baseball.ranking.dto;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.LeagueType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankingResponseDto {

	// 프론트 응답용 dto
	private LeagueType leagueType;

	private int rank;
	private Long teamId;
	private String teamName;
	private String teamCode;

	private int matchCount; // 경기수 ( 취소 / 미경기 제외 )

	private int winCount; // 승리
	private int loseCount; // 패배
	private int drawCount; // 무승부

	private double winRate; // 승률 = win / (win+lose)

	/**
	 * TeamRankingStat 객체와 등수를 기반으로 DTO를 생성하는 정적 팩토리 메서드
	 *
	 * @param rank    산출된 순위
	 * @param stat    랭킹 통계 데이터 객체
	 * @param winRate 계산된 승률
	 * @return TeamRankingResponseDto
	 */
	public static TeamRankingResponseDto of(int rank, TeamRankingStat stat, double winRate) {
		Objects.requireNonNull(stat, "stat must not be null");
		return TeamRankingResponseDto.builder()
				.leagueType(stat.getLeagueType())
				.rank(rank)
				.teamId(stat.getTeamId())
				.teamName(stat.getTeamName())
				.teamCode(stat.getTeamCode() != null ? stat.getTeamCode().getDescriptiveCode() : null)
				.matchCount(stat.getMatchCount())
				.winCount(stat.getWinCount())
				.loseCount(stat.getLoseCount())
				.drawCount(stat.getDrawCount())
				.winRate(winRate)
				.build();
	}
}
