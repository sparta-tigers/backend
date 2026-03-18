package com.sparta.spartatigers.domain.matchAttendance.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceRequestDto;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceResponseDto;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceUpdateRequestDto;
import com.sparta.spartatigers.domain.matchAttendance.model.AttendanceImageType;
import com.sparta.spartatigers.domain.matchAttendance.model.MatchAttendance;
import com.sparta.spartatigers.domain.matchAttendance.repository.MatchAttendanceRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchAttendanceService {

	private final MatchAttendanceRepository matchAttendanceRepository;
	private final UserRepository userRepository;
	private final MatchRepository matchRepository;
	private final OcrService ocrService;

	@Transactional
	public MatchAttendanceResponseDto createAttendance (Long userId, MatchAttendanceRequestDto request) {

		User user = userRepository.findById(userId).orElseThrow(()-> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
		Match match = matchRepository.findById(request.matchId()).orElseThrow(()-> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));

		MatchAttendance attendance = MatchAttendance.create(
			user,
			match,
			request.contents(),
			request.seat()
		);

		if(request.imageUrls() != null && !request.imageUrls().isEmpty()) {
			for (String imageUrl : request.imageUrls()) {
				attendance.addImage(imageUrl, AttendanceImageType.NORMAL);
			}
		}

		MatchAttendance saved = matchAttendanceRepository.save(attendance);

		return MatchAttendanceResponseDto.from(saved);
	}

	@Transactional(readOnly = true)
	public MatchAttendanceResponseDto getAttendance(Long attendanceId) {
		MatchAttendance attendance = matchAttendanceRepository.findById(attendanceId).orElseThrow(()-> new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_NOT_FOUND));
		return MatchAttendanceResponseDto.from(attendance);
	}

	@Transactional(readOnly = true)
	public Page< MatchAttendanceResponseDto> getAllAttendance(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(
			page-1, size, Sort.by(Sort.Direction.DESC, "createdAt")
		);
		Page<MatchAttendance> attendances = matchAttendanceRepository.findAllByUserId(userId, pageable);
		return attendances.map(MatchAttendanceResponseDto::from);
	}

	@Transactional
	public MatchAttendanceResponseDto updateAttendance(Long userId, Long attendanceId, MatchAttendanceUpdateRequestDto request) {
		MatchAttendance attendance = matchAttendanceRepository.findById(attendanceId).orElseThrow(()->new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_NOT_FOUND));

		if(!attendance.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_FORBIDDEN);
		}

		attendance.update(request.contents(), request.seat());

		if(request.imageUrls() !=null) {
			attendance.clearImages();
			for(String imageUrl : request.imageUrls()) {
				attendance.addImage(imageUrl, AttendanceImageType.NORMAL);
			}
		}

		return MatchAttendanceResponseDto.from(attendance);
	}

	@Transactional
	public void deleteAttendance(Long userId, Long attendanceId) {
		MatchAttendance attendance = matchAttendanceRepository.findById(attendanceId).orElseThrow(()->new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_NOT_FOUND));

		if(!attendance.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_FORBIDDEN);
		}

		matchAttendanceRepository.delete(attendance);
	}
}
