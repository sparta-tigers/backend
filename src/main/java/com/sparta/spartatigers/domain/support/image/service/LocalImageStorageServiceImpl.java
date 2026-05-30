package com.sparta.spartatigers.domain.support.image.service;

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

import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalImageStorageServiceImpl implements ImageStorageService {

    // 허용된 이미지 MIME 타입
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif");

    // 허용된 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif");

    // 설정 가능한 영구 저장 경로
    private final String uploadDir;

    // [FIX] 문제 3: uploadDirPath를 필드로 캐싱 — 매 업로드/삭제마다
    // Paths.get().toAbsolutePath().normalize() 재계산 방지
    private final Path uploadDirPath;

    // 안전한 파일명 패턴 (영숫자와 일부 특수문자만 허용)
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    public LocalImageStorageServiceImpl(@Value("${image.storage.path:./uploads}") String uploadPath) {
        // 생성자에서 디렉토리 경로 설정 및 생성 (절대 경로로 변환하여 Tomcat 등의 임시 경로 혼선 방지)
        try {
            Path uploadDirectory = Paths.get(uploadPath).toAbsolutePath().normalize();
            this.uploadDir = uploadDirectory.toString();
            this.uploadDirPath = uploadDirectory; // [FIX] 필드에 캐싱

            Files.createDirectories(uploadDirectory);

            // 쓰기 권한 확인
            if (!Files.isWritable(uploadDirectory)) {
                throw new RuntimeException("업로드 디렉토리에 쓰기 권한이 없습니다: " + uploadDir);
            }

            log.info("업로드 디렉토리 초기화 완료: {}", uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadPath, e);
        }
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        log.info("=== 이미지 업로드 시작 (파일 수: {}) ===", images != null ? images.size() : 0);
        log.debug("images 파라미터: {}", images);
        log.debug("uploadDir: {}", uploadDir);

        if (images == null || images.isEmpty()) {
            log.info("이미지 리스트가 null이거나 비어있음");
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        List<Path> savedFiles = new ArrayList<>(); // 저장된 파일 경로 추적

        // [FIX] 문제 3: 생성자에서 이미 Files.createDirectories()로 초기화 완료
        // 매 업로드마다 directory.mkdirs()를 반복 호출하는 불필요한 중복 제거

        try {
            for (MultipartFile file : images) {
                log.debug("처리 중인 파일: {}, 크기: {}, isEmpty: {}",
                        file.getOriginalFilename(), file.getSize(), file.isEmpty());

                if (file.isEmpty())
                    continue;

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
                    throw new InvalidRequestException(ExceptionCode.INVALID_FILE_FORMAT);
                }

                // 보안 검증: 파일 확장자 확인
                String fileExtension = getFileExtension(originalFilename).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                    log.error("허용되지 않는 파일 확장자: {} (파일: {})", fileExtension, originalFilename);
                    throw new InvalidRequestException(ExceptionCode.INVALID_FILE_EXTENSION);
                }

                // 보안 검증: 실제 이미지 파일인지 확인
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        log.error("이미지 디코딩 실패: {}", originalFilename);
                        throw new InvalidRequestException(ExceptionCode.INVALID_FILE_FORMAT);
                    }
                    log.info("이미지 검증 성공: {}x{}", image.getWidth(), image.getHeight());
                }

                // 파일명에서 디렉토리 경로 제거 및 안전한 문자만 남기기
                String baseName = sanitizeFilename(originalFilename);
                String fileName = UUID.randomUUID().toString() + "_" + baseName;

                // [FIX] 문제 3: 캐싱된 uploadDirPath 사용 (매 반복 Paths.get().normalize() 재계산 제거)
                Path destinationPath = uploadDirPath.resolve(fileName);

                // 보안 검증: 경로 순회 공격 방지
                if (!destinationPath.startsWith(uploadDirPath)) {
                    log.error("경로 순회 공격 시도 감지: {}", destinationPath);
                    throw new InvalidRequestException(ExceptionCode.INVALID_FILE_FORMAT);
                }

                // [FIX] 문제 2: UUID prefix로 충돌 가능성은 극히 낮지만,
                // 정책상 동일 파일명 존재 시 덮어쓰지 않고 실패(FileAlreadyExistsException)시킵니다.
                // 의도된 동작: 업로드 실패 → 클라이언트에 오류 반환
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationPath);
                }

                // 저장된 파일 경로 추적
                savedFiles.add(destinationPath);

                // 컨트롤러 엔드포인트 URL 반환 (/api/images/{fileName})
                imageUrls.add("/api/images/" + fileName);
                log.info("파일 저장 성공: {}", fileName);
            }
            // [FIX] 문제 1: catch (IOException e) → catch (Exception e)
            // 기존에는 MIME/확장자/이미지 디코딩/경로 순회 검증에서 발생하는 RuntimeException을 잡지 않아
            // N번째 파일 검증 실패 시 이전 N-1개 저장 파일이 롤백되지 않고 디스크에 남는 문제 수정
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생, 저장된 파일 롤백 시작", e);

            // 저장된 파일들 삭제 (롤백)
            for (Path savedFile : savedFiles) {
                try {
                    if (Files.deleteIfExists(savedFile)) {
                        log.info("롤백: 파일 삭제 성공 - {}", savedFile);
                    }
                } catch (IOException deleteException) {
                    log.error("롤백: 파일 삭제 실패 - {}", savedFile, deleteException);
                }
            }

            // [FIX] 문제 2: 예외 원인 보존 및 인터럽트 복구
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("이미지 저장 중 인터럽트가 발생했습니다.", e);
            }
            if (e instanceof InvalidRequestException) {
                throw (InvalidRequestException) e;
            }
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }

        log.debug("최종 imageUrls: {}", imageUrls);
        log.info("=== 이미지 업로드 종료 (성공: {}) ===", imageUrls.size());
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
                if (imageUrl == null)
                    continue;
                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                // [FIX] 문제 3: 캐싱된 uploadDirPath 사용
                Path filePath = uploadDirPath.resolve(fileName).normalize();

                // 경로 순회 공격 방지 확인
                if (!filePath.startsWith(uploadDirPath)) {
                    log.warn("잘못된 파일 삭제 시도: {}", imageUrl);
                    continue;
                }

                // [FIX] 문제 1: deleteIfExists 통일 및 삭제 실패 로깅 최소화
                if (Files.deleteIfExists(filePath)) {
                    log.info("파일 삭제 성공: {}", fileName);
                } else {
                    log.debug("삭제 대상 파일이 존재하지 않음: {}", fileName);
                }
            } catch (Exception e) {
                log.error("파일 삭제 중 오류 발생: {}", imageUrl, e);
            }
        }
    }
}
