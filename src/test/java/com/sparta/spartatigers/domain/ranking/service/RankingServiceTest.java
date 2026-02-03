package com.sparta.spartatigers.domain.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.model.MatchResult;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.ranking.dto.TeamRankingResponseDto;
import com.sparta.spartatigers.domain.team.model.Team;
import com.sparta.spartatigers.domain.team.repository.TeamRepository;

@DataJpaTest
@Import(TeamRankingService.class)
@ActiveProfiles("test")
public class RankingServiceTest {

	@Autowired
	private TeamRankingService rankingService;

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private TeamRepository teamRepository;

	private Team teamA;
	private Team teamB;
	private Team teamC;

	@BeforeEach
	void setUp() {
		// 테스트용 팀 세팅
		teamA = teamRepository.save(Team.of("A팀"));
		teamB = teamRepository.save(Team.of("B팀"));
		teamC = teamRepository.save(Team.of("C팀"));

		// 테스트용 경기 세팅
		matchRepository.save(
			Match.builder()
				.homeTeam(teamA).awayTeam(teamB).matchResult(MatchResult.HOME_WIN).build()
		);

		matchRepository.save(
			Match.builder()
				.homeTeam(teamA).awayTeam(teamC).matchResult(MatchResult.AWAY_WIN).build()
		);

		matchRepository.save(
			Match.builder()
				.homeTeam(teamB).awayTeam(teamC).matchResult(MatchResult.DRAW).build()
		);

		matchRepository.save(
			Match.builder()
				.homeTeam(teamA).awayTeam(teamB).matchResult(MatchResult.NOT_PLAYED).build()
		);
	}

	@Test
	void 랭킹은_승률_기준으로_정렬한다() {

		// when
		List<TeamRankingResponseDto> rankings = rankingService.getRankings();

		rankings.forEach(r ->
			System.out.println(
				r.getTeamName()
					+ " | W:" + r.getWinCount()
					+ " L:" + r.getLoseCount()
					+ " D:" + r.getDrawCount()
					+ " | WR:" + r.getWinRate()
			)
		);

		//then
		assertThat(rankings).hasSize(3);

		TeamRankingResponseDto first = rankings.get(0);
		TeamRankingResponseDto second = rankings.get(1);
		TeamRankingResponseDto third = rankings.get(2);


		assertThat(first.getRank()).isEqualTo(1);
		assertThat(first.getTeamName()).isEqualTo("C팀");
		assertThat(first.getWinCount()).isEqualTo(1);
		assertThat(first.getLoseCount()).isEqualTo(0);
		assertThat(first.getDrawCount()).isEqualTo(1);

		assertThat(second.getRank()).isEqualTo(2);
		assertThat(second.getTeamName()).isEqualTo("A팀");

		assertThat(third.getRank()).isEqualTo(3);
		assertThat(third.getTeamName()).isEqualTo("B팀");
	}

	@Test
	void NOT_PLAYED_경기는_집계에서_제외된다() {
		//when
		List<TeamRankingResponseDto> rankings = rankingService.getRankings();

		//then
		TeamRankingResponseDto teamAStat = rankings.stream().filter(r->r.getTeamName().equals("A팀")).findFirst().orElseThrow();

		assertThat(teamAStat.getMatchCount()).isEqualTo(2);
	}
}
