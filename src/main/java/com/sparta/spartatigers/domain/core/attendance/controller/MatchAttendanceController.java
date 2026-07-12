package com.sparta.spartatigers.domain.core.attendance.controller;

import com.sparta.spartatigers.domain.core.attendance.dto.MatchAttendanceRequestDto;
import com.sparta.spartatigers.domain.core.attendance.dto.MatchAttendanceResponseDto;
import com.sparta.spartatigers.domain.core.attendance.dto.MatchAttendanceUpdateRequestDto;
import com.sparta.spartatigers.domain.core.attendance.dto.TicketOcrResponseDto;
import com.sparta.spartatigers.domain.core.attendance.service.MatchAttendanceService;
import com.sparta.spartatigers.domain.core.attendance.service.OcrService;
import com.sparta.spartatigers.domain.support.image.service.ImageStorageService;
import com.sparta.spartatigers.global.aop.Auth;
import com.sparta.spartatigers.global.aop.TokenClaim;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import com.sparta.spartatigers.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
        @Auth TokenClaim tokenClaim,
        @RequestPart(value = "images", required = true) MultipartFile ticket
    ) {
        log.info("티켓 좌석 추출 시작 ==");
        if (ticket == null || ticket.isEmpty()) {
            throw new InvalidRequestException(
                ExceptionCode.INVALID_TICKET_IMAGE
            );
        }
        try {
            String fullText = ocrService.extractTextFromImage(ticket);
            String parsedSeat = ocrService.parseSeatInfo(fullText);

            List<String> imageUrls = imageStorageService.uploadImages(
                List.of(ticket)
            );
            String uploadedImageUrl = imageUrls.isEmpty()
                ? null
                : imageUrls.get(0);

            TicketOcrResponseDto dto = TicketOcrResponseDto.from(
                uploadedImageUrl,
                parsedSeat
            );

            return ApiResponse.success(dto);
        } catch (Exception e) {
            log.error(
                "직관기록 - 티켓 OCR 중 오류 발생 / 파일명 ={}",
                ticket.getOriginalFilename(),
                e
            );
            throw new InvalidRequestException(ExceptionCode.TICKET_OCR_ERROR);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MatchAttendanceResponseDto> createAttendance(
        @Auth TokenClaim tokenClaim,
        @RequestPart(
            value = "request",
            required = true
        ) @Valid MatchAttendanceRequestDto request,
        @RequestPart(value = "images", required = false) List<
            MultipartFile
        > images
    ) {
        Long userId = tokenClaim.getUserId();

        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            uploadedImageUrls = imageStorageService.uploadImages(images);
        }

        try {
            MatchAttendanceResponseDto response =
                matchAttendanceService.createAttendance(
                    userId,
                    request,
                    uploadedImageUrls
                );
            return ApiResponse.created(response);
        } catch (Exception e) {
            if (!uploadedImageUrls.isEmpty()) {
                log.error(
                    "직관기록 이미지 저장 실패, 업로드 된 파일 롤백 : {}",
                    uploadedImageUrls
                );
                imageStorageService.deleteImages(uploadedImageUrls);
            }
            throw e;
        }
    }

    @GetMapping("/{attendanceId}")
    public ApiResponse<MatchAttendanceResponseDto> getAttendance(
        @Auth TokenClaim tokenClaim,
        @PathVariable Long attendanceId
    ) {
        Long userId = tokenClaim.getUserId();
        return ApiResponse.success(
            matchAttendanceService.getAttendance(userId, attendanceId)
        );
    }

    @GetMapping("/my")
    public ApiResponse<Page<MatchAttendanceResponseDto>> getMyAttendances(
        @Auth TokenClaim tokenClaim,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = tokenClaim.getUserId();
        return ApiResponse.success(
            matchAttendanceService.getAllAttendance(userId, page, size)
        );
    }

    @GetMapping("/my/match/{matchId}")
    public ApiResponse<MatchAttendanceResponseDto> getMyAttendanceByMatchId(
        @Auth TokenClaim tokenClaim,
        @PathVariable Long matchId
    ) {
        Long userId = tokenClaim.getUserId();
        MatchAttendanceResponseDto response =
            matchAttendanceService.getAttendanceByMatchId(userId, matchId);
        return ApiResponse.success(response);
    }

    @PatchMapping(
        value = "/{attendanceId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<MatchAttendanceResponseDto> updateAttendance(
        @Auth TokenClaim tokenClaim,
        @PathVariable Long attendanceId,
        @RequestPart(
            value = "request"
        ) @Valid MatchAttendanceUpdateRequestDto request,
        @RequestPart(value = "images", required = false) List<
            MultipartFile
        > newImages
    ) {
        Long userId = tokenClaim.getUserId();

        List<String> uploadedImageUrls = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            uploadedImageUrls = imageStorageService.uploadImages(newImages);
        }

        try {
            MatchAttendanceResponseDto response =
                matchAttendanceService.updateAttendance(
                    userId,
                    attendanceId,
                    request,
                    uploadedImageUrls
                );
            return ApiResponse.success(response);
        } catch (Exception e) {
            if (!uploadedImageUrls.isEmpty()) {
                log.error(
                    "직관기록 이미지 수정 실패, 업로드 된 파일 롤백 : {}",
                    uploadedImageUrls
                );
                imageStorageService.deleteImages(uploadedImageUrls);
            }
            throw e;
        }
    }

    @DeleteMapping("/{attendanceId}")
    public ApiResponse<?> deleteAttendance(
        @Auth TokenClaim tokenClaim,
        @PathVariable Long attendanceId
    ) {
        Long userId = tokenClaim.getUserId();
        matchAttendanceService.deleteAttendance(userId, attendanceId);
        return ApiResponse.success("");
    }

    @GetMapping("/count")
    public ApiResponse<Long> getAttendanceCount(
        @Auth TokenClaim tokenClaim,
        @RequestParam(required = false) Integer year
    ) {
        Long userId = tokenClaim.getUserId();
        return ApiResponse.success(
            matchAttendanceService.getAttendanceCount(userId, year)
        );
    }
}
