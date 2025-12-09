package com.sparta.spartatigers.domain.ticketalarm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.CreateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.request.UpdateTicketAlarmRequestDto;
import com.sparta.spartatigers.domain.ticketalarm.dto.response.TicketAlarmResponseDto;
import com.sparta.spartatigers.domain.ticketalarm.model.TeamBookingPolicy;
import com.sparta.spartatigers.domain.ticketalarm.model.TicketAlarm;
import com.sparta.spartatigers.domain.ticketalarm.model.applyScope;
import com.sparta.spartatigers.domain.ticketalarm.model.baseType;
import com.sparta.spartatigers.domain.ticketalarm.repository.TeamBookingPolicyRepository;
import com.sparta.spartatigers.domain.ticketalarm.repository.TicketAlarmRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketAlarmService {

	private final UserRepository userRepository;
	private final MatchRepository matchRepository;
	private final TeamBookingPolicyRepository bookingPolicyRepository;
	private final TicketAlarmRepository ticketAlarmRepository;

	// TODO : 지난 알림 삭제해 말아???

	@Transactional
	public TicketAlarmResponseDto createAlarm(Long userId, CreateTicketAlarmRequestDto request) {
		// 1. 구단 예매 정책 확인하기
		validatePreAlarmTime(request.getPreAlarmTime());
		TeamBookingPolicy bookingPolicy;

		if (request.getMembership() == null || request.getMembership().isBlank()) {
			bookingPolicy = bookingPolicyRepository.findDefaultPolicyByTeamId(request.getTeamId());
		} else {
			bookingPolicy = bookingPolicyRepository.findByTeamIdAndMembership(request.getTeamId(),
				request.getMembership());
		}

		if(bookingPolicy == null) {
			throw new InvalidRequestException(ExceptionCode.POLICY_NOT_FOUND);
		}

		// 2. 예매 오픈시간 찾기
		Match targetMatch = matchRepository.findByMatchId(request.getMatchId())
			.orElseThrow(()-> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));
		LocalDateTime openBookingTime = calculateOpenBookingTime(bookingPolicy, targetMatch);

		// 3. 푸시 알림 시간 설정
		LocalDateTime alarmTime = openBookingTime.minusMinutes(request.getPreAlarmTime());
		validateAlarmTime(alarmTime);

		// 4. 엔티티 생성 & 저장
		User findUser = userRepository.findByIdOrElseThrow(userId);
		TicketAlarm alarm = TicketAlarm.of(
			findUser,
			targetMatch,
			bookingPolicy,
			request.getPreAlarmTime(),
			alarmTime,
			openBookingTime
		);
		ticketAlarmRepository.save(alarm);

		return TicketAlarmResponseDto.from(alarm);
	}

	public Page<TicketAlarmResponseDto> getAllAlarms(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(
			page, size, Sort.by(Sort.Direction.ASC, "alarmTime")
		);
		return ticketAlarmRepository.findByUserId(userId, pageable)
			.map(TicketAlarmResponseDto::from);
	}

	@Transactional
	public TicketAlarmResponseDto updateAlarm(Long userId, Long alarmId, UpdateTicketAlarmRequestDto request) {
		// 유저의 알람이 맞는지 (해당 알람을 만든 사람이 맞는지)
		TicketAlarm alarm = ticketAlarmRepository.findById(alarmId)
			.orElseThrow(()-> new InvalidRequestException(ExceptionCode.ALARM_NOT_FOUND));

		if(!alarm.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.AUTHORIZATION_ERROR);
		}

		// 요청 멤버쉽 없으면 기존과 동일
		TeamBookingPolicy currentPolicy = alarm.getTeamBookingPolicy();
		TeamBookingPolicy newPolicy = currentPolicy;

		if(request.getMemberShip() != null && !request.getMemberShip().equals(currentPolicy.getMembership())) {
			newPolicy = bookingPolicyRepository.findByTeamIdAndMembership(alarm.getMatch().getHomeTeam().getId(), request.getMemberShip());

			if (newPolicy == null) {
				throw new InvalidRequestException(ExceptionCode.POLICY_NOT_FOUND);
			}
		}

		// 예매 오픈 시간 다시 계산
		LocalDateTime openBookingTime = calculateOpenBookingTime(newPolicy, alarm.getMatch());
		Integer newPreAlarmTime =
			request.getPreAlarmTime() != null ? request.getPreAlarmTime() : alarm.getMinusBefore();
		validatePreAlarmTime(newPreAlarmTime);
		LocalDateTime newAlarmTime = openBookingTime.minusMinutes(newPreAlarmTime);
		validateAlarmTime(newAlarmTime);

		// 엔티티 업데이트
		alarm.update(newPolicy, newPreAlarmTime, openBookingTime, newAlarmTime);

		return TicketAlarmResponseDto.from(alarm);
	}

	@Transactional
	public void deleteAlarm (Long userId, Long alarmId) {
		TicketAlarm alarm = ticketAlarmRepository.findById(alarmId)
			.orElseThrow(()->new InvalidRequestException(ExceptionCode.ALARM_NOT_FOUND));

		if(!alarm.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.AUTHORIZATION_ERROR);
		}

		ticketAlarmRepository.delete(alarm);
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
				.orElseThrow(()-> new InvalidRequestException(ExceptionCode.BOOKING_SCHEDULE_NOT_FOUND));

			baseTime = firstHomeSeriesMatch.getMatchTime();

		} else {
			throw new InvalidRequestException(ExceptionCode.BOOKING_SCHEDULE_NOT_FOUND);
		}

		// 2. 기준 경기 시간에서 예매 오픈 날짜, 시간 찾기
		LocalDate openDate = baseTime.toLocalDate().minusDays(bookingPolicy.getOpenDaysBefore());
		LocalDateTime openDateTime = LocalDateTime.of(openDate, LocalTime.of(bookingPolicy.getOpenHourOfDay(),
			bookingPolicy.getOpenMinOfDay(), 0, 0));

		return openDateTime;
	}

	// 알람은 세시간 전까지만!
	private void validatePreAlarmTime(Integer minutes) {
		if (minutes == null || minutes <=0 || minutes >180) {
			throw new InvalidRequestException(ExceptionCode.INVALID_PRE_ALARM_TIME);
		}
	}

	// 알람시간이 이미 지났는지 검증
	private void validateAlarmTime (LocalDateTime alarmTime) {
		if(alarmTime.isBefore(LocalDateTime.now())) {
			throw new InvalidRequestException(ExceptionCode.ALARM_TIME_ALREADY_PASSED);
		}
	}

}
