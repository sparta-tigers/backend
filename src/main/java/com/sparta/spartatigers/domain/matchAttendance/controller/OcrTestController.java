package com.sparta.spartatigers.domain.matchAttendance.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.spartatigers.domain.matchAttendance.service.OcrService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrTestController {

	private final OcrService ocrService;

	@PostMapping("/test")
	public String testOcr(
		@RequestParam MultipartFile file
	) throws IOException {
		System.out.println("1. 파일 업로드 : "+ file.getOriginalFilename());

		String fullText = ocrService.extractTextFromImage(file);
		System.out.println("2. OCR 추출 원본 텍스트 : ");
		System.out.println(fullText);

		String seatInfo = ocrService.parseSeatInfo(fullText);
		System.out.println("3. 좌석 파싱 결과 : ");
		System.out.println(seatInfo);

		return "원문 : \n" + fullText + "\n\n 파싱 결과 : \n" + seatInfo;
	}
}
