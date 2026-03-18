package com.sparta.spartatigers.domain.matchAttendance.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.image.service.ImageStorageService;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceRequestDto;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceResponseDto;
import com.sparta.spartatigers.domain.matchAttendance.dto.MatchAttendanceUpdateRequestDto;
import com.sparta.spartatigers.domain.matchAttendance.dto.TicketOcrResponseDto;
import com.sparta.spartatigers.domain.matchAttendance.service.MatchAttendanceService;
import com.sparta.spartatigers.domain.matchAttendance.service.OcrService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/attendances")
public class MatchAttendanceController {

	private final MatchAttendanceService matchAttendanceService;
	private final OcrService ocrService;
	private final ImageStorageService imageStorageService;

	@PostMapping("/ticket")
	public ApiResponse<TicketOcrResponseDto> uploadTicketAndOcr(
		@RequestPart(value = "images", required = true) MultipartFile ticket
	) {
		if (ticket == null || ticket.isEmpty()) {
			throw new InvalidRequestException(ExceptionCode.INVALID_TICKET_IMAGE);
		}
		try {
			List<String> imageUrls = imageStorageService.uploadImages(List.of(ticket));
			String uploadedImageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

			String fullText = ocrService.extractTextFromImage(ticket);
			String parsedSeat = ocrService.parseSeatInfo(fullText);
			TicketOcrResponseDto dto = TicketOcrResponseDto.from(uploadedImageUrl, parsedSeat);

			return ApiResponse.success(dto);
		} catch (Exception e) {
			log.error("직관기록 - 티켓 OCR 중 오류 발생 / 파일명 ={}", ticket.getOriginalFilename(), e);
			throw new InvalidRequestException(ExceptionCode.TICKET_OCR_ERROR);
		}

	}

	@PostMapping
	public ApiResponse<MatchAttendanceResponseDto> createAttendance(
		@Valid @RequestParam MatchAttendanceRequestDto request,
		@Auth TokenClaim tokenClaim
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.created(matchAttendanceService.createAttendance(userId, request));
	}

	@GetMapping("/{attendanceId}")
	public ApiResponse<MatchAttendanceResponseDto> getAttendance(
		@PathVariable Long attendanceId
	) {
		return ApiResponse.success(matchAttendanceService.getAttendance(attendanceId));
	}

	@GetMapping("/my")
	public ApiResponse<Page<MatchAttendanceResponseDto>> getMyAttendances(
		@Auth TokenClaim tokenClaim,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(matchAttendanceService.getAllAttendance(userId, page, size));
	}

	@PatchMapping("/{attendanceId}")
	public ApiResponse<MatchAttendanceResponseDto> updateAttendance (
		@Auth TokenClaim tokenClaim,
		@PathVariable Long attendanceId,
		@RequestParam MatchAttendanceUpdateRequestDto request
	) {
		Long userId = tokenClaim.getUserId();
		return ApiResponse.success(matchAttendanceService.updateAttendance(userId, attendanceId, request));

	}

	@DeleteMapping("/{attendanceId}")
	public ApiResponse<?> deleteAttendance(
		@Auth TokenClaim tokenClaim,
		@PathVariable Long attendanceId
	) {
		Long userId = tokenClaim.getUserId();
		matchAttendanceService.deleteAttendance(userId,attendanceId);
		return ApiResponse.success("");
	}


}
