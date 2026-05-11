package com.sparta.spartatigers.domain.liveboard.matchAttendance.controller;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.spartatigers.domain.liveboard.matchAttendance.service.OcrService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Profile("local")
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrTestController {

	private final OcrService ocrService;

	@PostMapping("/test")
	public String testOcr(
		@RequestParam MultipartFile file
	) throws IOException {
		log.info("1. 파일 업로드 : {}", file.getOriginalFilename());

		String fullText = ocrService.extractTextFromImage(file);
		log.info("2. OCR 추출 완료");

		String seatInfo = ocrService.parseSeatInfo(fullText);
		log.info("3. 좌석 파싱 결과 : {}", seatInfo);

		return "파싱 결과 : \n" + seatInfo;
	}
}
