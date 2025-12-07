package com.sparta.spartatigers.domain.ticketalarm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.CreateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.response.TicketAlarmResponseDto;
import com.sparta.spartatigers.domain.ticketalarm.model.TeamBookingPolicy;
import com.sparta.spartatigers.domain.ticketalarm.model.TicketAlarm;
import com.sparta.spartatigers.domain.ticketalarm.model.applyScope;
import com.sparta.spartatigers.domain.ticketalarm.model.baseType;
import com.sparta.spartatigers.domain.ticketalarm.repository.TeamBookingPolicyRepository;
import com.sparta.spartatigers.domain.ticketalarm.repository.TicketAlarmRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketAlarmService {

	private final UserRepository userRepository;
	private final MatchRepository matchRepository;
	private final TeamBookingPolicyRepository bookingPolicyRepository;
	private final TicketAlarmRepository ticketAlarmRepository;

	// TODO : 익셉션 정리
	public TicketAlarmResponseDto createAlarm(Long userId, CreateTicketAlarmRequestDto request) {
		// 1. 구단 예매 정책 확인하기
		TeamBookingPolicy bookingPolicy = bookingPolicyRepository.findByTeamIdAndMembership(request.getTeamId(),
			request.getMembership());

		// 2. 예매 오픈시간 찾기
		Match targetMatch = matchRepository.findByMatchId(request.getMatchId()).orElseThrow();
		LocalDateTime openBookingTime = calculateOpenBookingTime(bookingPolicy, targetMatch);

		// 3. 푸시 알림 시간 설정
		LocalDateTime alarmTime = openBookingTime.minusMinutes(request.getPreAlarmTime());

		// 4. 엔티티 생성 & 저장
		User findUser = userRepository.findByIdOrElseThrow(userId);
		TicketAlarm alarm = TicketAlarm.of(
			findUser,
			targetMatch,
			bookingPolicy,
			request.getPreAlarmTime(),
			alarmTime
		);
		ticketAlarmRepository.save(alarm);

		return TicketAlarmResponseDto.from(alarm, openBookingTime);
	}

	// ----------------- Util 메서드
	// 예매 오픈 시간 구하기
	private LocalDateTime calculateOpenBookingTime (TeamBookingPolicy bookingPolicy, Match targetMatch)  {

		LocalDateTime baseTime;

		// 1. 기준 경기시간 찾기
		if (bookingPolicy.getApplyScope().equals(applyScope.SINGLE_MATCH)) {

			// 일반
			baseTime = targetMatch.getMatchTime();

		} else if (bookingPolicy.getApplyScope().equals(applyScope.HOME_SERIES)
			&& bookingPolicy.getBaseType().equals(baseType.HOME_SERIES_FIRST_MATCH)) {

			// 삼성, 롯데 (홈시리즈 3연전 기준)
			Long awayTeamId = targetMatch.getAwayTeam().getId();
			Long homeTeamId = targetMatch.getHomeTeam().getId();
			LocalDate matchDate = targetMatch.getMatchTime().toLocalDate();

			int seriesCount = bookingPolicy.getSeriesCount();

			LocalDateTime from = matchDate.minusDays(seriesCount-1).atStartOfDay();
			LocalDateTime to = matchDate.plusDays(seriesCount-1).atTime(23,59,59);

			Match firstHomeSeriesMatch = matchRepository.findFirstHomeSeriesMatch(awayTeamId, homeTeamId, from, to)
				.orElseThrow(()-> new RuntimeException());

			baseTime = firstHomeSeriesMatch.getMatchTime();

		} else {
			throw new RuntimeException();
		}

		// 2. 기준 경기 시간에서 예매 오픈 날짜, 시간 찾기
		LocalDate openDate = baseTime.toLocalDate().minusDays(bookingPolicy.getOpenDaysBefore());
		LocalDateTime openDateTime = LocalDateTime.of(openDate, LocalTime.of(bookingPolicy.getOpenHourOfDay(),
			bookingPolicy.getOpenMinOfDay(), 0, 0));

		return openDateTime;
	}


}
