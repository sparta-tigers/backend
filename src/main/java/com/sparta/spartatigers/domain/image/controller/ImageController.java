package com.sparta.spartatigers.domain.image.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/images")
@Slf4j
public class ImageController {
    
    private String uploadDirectory;
 
    public ImageController(@Value("${image.storage.path:./uploads}") String uploadPath) {
        this.uploadDirectory = java.nio.file.Paths.get(uploadPath).toAbsolutePath().normalize().toString();
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            // 업로드 디렉토리 경로 가져오기
            String uploadDir = getUploadDirectory();
            Path filePath = Paths.get(uploadDir, fileName).normalize();
            
            // 경로 순회 공격 방지 확인
            Path uploadDirPath = Paths.get(uploadDir).normalize();
            if (!filePath.startsWith(uploadDirPath)) {
                log.warn("잘못된 파일 접근 시도: {}", fileName);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 파일 존재 확인
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없거나 읽을 수 없음: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // MIME 타입 결정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("파일 읽기 오류: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 업로드 디렉토리 경로를 가져옵니다. Spring 속성으로 주입된 값을 사용하며, 슬래시 정규화를 보장합니다.
     */
    private String getUploadDirectory() {
        return uploadDirectory.endsWith("/") ? uploadDirectory : uploadDirectory + "/";
    }
}
