package com.sparta.spartatigers.domain.image.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalImageStorageServiceImpl implements ImageStorageService {
    
    // 로컬 저장 경로 (프로젝트 루트의 uploads 폴더 등)
    private final String uploadDir = System.getProperty("java.io.tmpdir") + "/yaguniv/uploads/";

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        log.info("=== 이미지 업로드 시작 ===");
        log.info("images 파라미터: {}", images);
        log.info("uploadDir: {}", uploadDir);
        
        if (images == null || images.isEmpty()) {
            log.info("이미지 리스트가 null이거나 비어있음");
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            log.info("디렉토리 생성: {}", directory.getAbsolutePath());
            directory.mkdirs();
        }

        for (MultipartFile file : images) {
            log.info("처리 중인 파일: {}, 크기: {}, isEmpty: {}", 
                file.getOriginalFilename(), file.getSize(), file.isEmpty());
            
            if (file.isEmpty()) continue;
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File destination = new File(uploadDir + fileName);
                // transferTo는 application.yml의 설정에 따라 임시 파일을 실제 경로로 안전하게 이동시킴
                file.transferTo(destination);
                
                // 임시 URL 반환 (로컬 정적 리소스 서빙 경로)
                imageUrls.add("/uploads/" + fileName);
                log.info("파일 저장 성공: {}", fileName);
            } catch (IOException e) {
                log.error("Failed to store image file", e);
                throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.");
            }
        }
        
        log.info("최종 imageUrls: {}", imageUrls);
        log.info("=== 이미지 업로드 종료 ===");
        return imageUrls;
    }
}
