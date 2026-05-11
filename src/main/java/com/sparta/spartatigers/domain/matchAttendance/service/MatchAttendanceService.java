package com.sparta.spartatigers.domain.liveboard.matchAttendance.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sparta.spartatigers.domain.image.service.ImageStorageService;
import com.sparta.spartatigers.domain.liveboard.match.model.Match;
import com.sparta.spartatigers.domain.liveboard.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.dto.MatchAttendanceRequestDto;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.dto.MatchAttendanceResponseDto;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.dto.MatchAttendanceUpdateRequestDto;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.model.AttendanceImage;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.model.AttendanceImageType;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.model.MatchAttendance;
import com.sparta.spartatigers.domain.liveboard.matchAttendance.repository.MatchAttendanceRepository;
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
	private final ImageStorageService imageStorageService;

	@Transactional
	public MatchAttendanceResponseDto createAttendance (Long userId, MatchAttendanceRequestDto request, List<String> uploadedImageUrls) {

		User user = userRepository.findById(userId).orElseThrow(()-> new InvalidRequestException(ExceptionCode.USER_NOT_FOUND));
		Match match = matchRepository.findById(request.matchId()).orElseThrow(()-> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));

		MatchAttendance attendance = MatchAttendance.create(
			user,
			match,
			request.contents(),
			request.seat()
		);

		if(uploadedImageUrls !=null && !uploadedImageUrls.isEmpty()) {
			for(String imageUrl : uploadedImageUrls) {
				attendance.addImage(imageUrl, AttendanceImageType.NORMAL);
			}
		}

		MatchAttendance saved = matchAttendanceRepository.save(attendance);

		return MatchAttendanceResponseDto.from(saved);
	}

	@Transactional(readOnly = true)
	public MatchAttendanceResponseDto getAttendance(Long userId, Long attendanceId) {
		MatchAttendance attendance = matchAttendanceRepository.findById(attendanceId).orElseThrow(()-> new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_NOT_FOUND));
		if(!attendance.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_FORBIDDEN);
		}

		return MatchAttendanceResponseDto.from(attendance);
	}

	@Transactional(readOnly = true)
	public Page< MatchAttendanceResponseDto> getAllAttendance(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(
			page-1, size, Sort.by(Sort.Direction.DESC, "createdAt")
		);
		Page<MatchAttendance> attendances = matchAttendanceRepository.findAllByUser_Id(userId, pageable);
		return attendances.map(MatchAttendanceResponseDto::from);
	}

	@Transactional
	public MatchAttendanceResponseDto updateAttendance(Long userId, Long attendanceId, MatchAttendanceUpdateRequestDto request, List<String> newImageUrls) {
		MatchAttendance attendance = matchAttendanceRepository.findById(attendanceId).orElseThrow(()->new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_NOT_FOUND));

		if(!attendance.getUser().getId().equals(userId)) {
			throw new InvalidRequestException(ExceptionCode.MATCH_ATTENDANCE_FORBIDDEN);
		}

		attendance.update(request.contents(), request.seat());

		// 수정 후 없어진 이미지 삭제 로직 ========
		// 기존 이미지 url
		List<String> oldImageUrls = attendance.getImages().stream().map(AttendanceImage::getImageUrl).toList();

		// 새로운 request에 없는 url 필터링
		List<String> urlsToDelete = oldImageUrls.stream().filter(oldUrl-> request.oldImageUrls()==null || !request.oldImageUrls().contains(oldUrl)).toList();

		// 삭제
		if(!urlsToDelete.isEmpty()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					imageStorageService.deleteImages(urlsToDelete);
				}
			});
		}

		// 이미지 갱신 로직 ========
		attendance.clearImages(); // 고아 객체 삭제

		if(request.oldImageUrls() != null) {
			for(String url : request.oldImageUrls()) {
				attendance.addImage(url, AttendanceImageType.NORMAL);
			}
		}

		if(newImageUrls != null) {
			for (String url : newImageUrls) {
				attendance.addImage(url,AttendanceImageType.NORMAL);
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

		List<String> urlsToDelete = attendance.getImages().stream().map(AttendanceImage::getImageUrl).toList();

		matchAttendanceRepository.delete(attendance);

		if(!urlsToDelete.isEmpty()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					imageStorageService.deleteImages(urlsToDelete);
				}
			});
		}
	}
}
