package com.sparta.spartatigers.domain.image.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalImageStorageServiceImpl implements ImageStorageService {
    
    // 허용된 이미지 MIME 타입
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/gif"
    );
    
    // 허용된 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif"
    );
    
    // 설정 가능한 영구 저장 경로
    private final String uploadPath;
    private final String uploadDir;
    
    // 안전한 파일명 패턴 (영숫자와 일부 특수문자만 허용)
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");
    
    public LocalImageStorageServiceImpl(@Value("${image.storage.path:./uploads}") String uploadPath) {
        // 생성자에서 디렉토리 경로 설정 및 생성
        this.uploadPath = uploadPath;
        this.uploadDir = uploadPath;
        try {
            Path uploadDirectory = Paths.get(uploadDir);
            Files.createDirectories(uploadDirectory);
            
            // 쓰기 권한 확인
            if (!Files.isWritable(uploadDirectory)) {
                throw new RuntimeException("업로드 디렉토리에 쓰기 권한이 없습니다: " + uploadDir);
            }
            
            log.info("업로드 디렉토리 초기화 완료: {}", uploadDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadDir, e);
        }
    }

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
        List<Path> savedFiles = new ArrayList<>(); // 저장된 파일 경로 추적
        
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            log.info("디렉토리 생성: {}", directory.getAbsolutePath());
            directory.mkdirs();
        }

        try {
            for (MultipartFile file : images) {
                log.info("처리 중인 파일: {}, 크기: {}, isEmpty: {}", 
                    file.getOriginalFilename(), file.getSize(), file.isEmpty());
                
                if (file.isEmpty()) continue;
                
                // 안전한 파일명 생성
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.trim().isEmpty()) {
                    log.warn("파일명이 비어있어 건너뜁니다");
                    continue;
                }
                
                // 보안 검증: MIME 타입 확인
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                    log.error("허용되지 않는 MIME 타입: {} (파일: {})", contentType, originalFilename);
                    throw new RuntimeException("허용되지 않는 파일 타입입니다: " + contentType);
                }
                
                // 보안 검증: 파일 확장자 확인
                String fileExtension = getFileExtension(originalFilename).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                    log.error("허용되지 않는 파일 확장자: {} (파일: {})", fileExtension, originalFilename);
                    throw new RuntimeException("허용되지 않는 파일 확장자입니다: " + fileExtension);
                }
                
                // 보안 검증: 실제 이미지 파일인지 확인
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        log.error("이미지 디코딩 실패: {}", originalFilename);
                        throw new RuntimeException("유효하지 않은 이미지 파일입니다: " + originalFilename);
                    }
                    log.info("이미지 검증 성공: {}x{}", image.getWidth(), image.getHeight());
                }
                
                // 파일명에서 디렉토리 경로 제거 및 안전한 문자만 남기기
                String baseName = sanitizeFilename(originalFilename);
                String fileName = UUID.randomUUID().toString() + "_" + baseName;
                
                // 최종 경로 생성 및 경로 순회 공격 방지 검증
                Path destinationPath = Paths.get(uploadDir, fileName).normalize();
                Path uploadDirPath = Paths.get(uploadDir).normalize();
                
                // 경로가 업로드 디렉토리 내에 있는지 확인
                if (!destinationPath.startsWith(uploadDirPath)) {
                    log.error("경로 순회 공격 시도 감지: {}", destinationPath);
                    throw new RuntimeException("잘못된 파일 경로입니다.");
                }
                
                File destination = destinationPath.toFile();
                
                // transferTo는 application.yml의 설정에 따라 임시 파일을 실제 경로로 안전하게 이동시킴
                file.transferTo(destination);
                
                // 저장된 파일 경로 추적
                savedFiles.add(destinationPath);
                
                // 컨트롤러 엔드포인트 URL 반환 (/api/v1/images/{fileName})
                imageUrls.add("/api/v1/images/" + fileName);
                log.info("파일 저장 성공: {}", fileName);
            }
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생, 저장된 파일 롤백 시작", e);
            
            // 저장된 파일들 삭제 (롤백)
            for (Path savedFile : savedFiles) {
                try {
                    if (Files.exists(savedFile)) {
                        Files.delete(savedFile);
                        log.info("롤백: 파일 삭제 성공 - {}", savedFile);
                    }
                } catch (IOException deleteException) {
                    log.error("롤백: 파일 삭제 실패 - {}", savedFile, deleteException);
                }
            }
            
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
        
        log.info("최종 imageUrls: {}", imageUrls);
        log.info("=== 이미지 업로드 종료 ===");
        return imageUrls;
    }
    
    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * 파일명을 안전하게 정제합니다.
     * 디렉토리 경로 제거, 위험한 문자 제거, 길이 제한
     */
    private String sanitizeFilename(String filename) {
        // 디렉토리 경로 제거
        String baseName = new File(filename).getName();
        
        // 확장자 분리
        int dotIndex = baseName.lastIndexOf('.');
        String nameWithoutExt = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
        String extension = dotIndex > 0 ? baseName.substring(dotIndex) : "";
        
        // 안전하지 않은 문자 제거 (영숫자, 점, 언더스코어, 하이픈만 허용)
        nameWithoutExt = SAFE_FILENAME_PATTERN.matcher(nameWithoutExt).replaceAll("");
        
        // 길이 제한 (너무 긴 파일명 방지)
        if (nameWithoutExt.length() > 50) {
            nameWithoutExt = nameWithoutExt.substring(0, 50);
        }
        
        // 빈 파일명 방지
        if (nameWithoutExt.isEmpty()) {
            nameWithoutExt = "file";
        }
        
        // 확장자도 안전하게 정제
        extension = extension.replaceAll("[^a-zA-Z0-9.]", "");
        if (extension.length() > 10) {
            extension = extension.substring(0, 10);
        }
        
        return nameWithoutExt + extension;
    }
    
    @Override
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        for (String imageUrl : imageUrls) {
            try {
                // "/api/v1/images/" 접두사 제거
                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                Path filePath = Paths.get(uploadDir, fileName).normalize();
                
                // 경로 순회 공격 방지 확인
                Path uploadDirPath = Paths.get(uploadDir).normalize();
                if (!filePath.startsWith(uploadDirPath)) {
                    log.warn("잘못된 파일 삭제 시도: {}", imageUrl);
                    continue;
                }
                
                File file = filePath.toFile();
                if (file.exists() && file.delete()) {
                    log.info("파일 삭제 성공: {}", fileName);
                } else {
                    log.warn("파일 삭제 실패: {}", fileName);
                }
            } catch (Exception e) {
                log.error("파일 삭제 중 오류 발생: {}", imageUrl, e);
            }
        }
    }
}
