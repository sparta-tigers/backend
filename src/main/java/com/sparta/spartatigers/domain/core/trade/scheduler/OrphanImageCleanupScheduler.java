package com.sparta.spartatigers.domain.core.trade.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartatigers.domain.core.trade.repository.ItemRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고아(Orphan) 이미지 파일을 주기적으로 정리하는 스케줄러.
 *
 * <p>
 * 문제 상황: 이미지 업로드 성공 후 아이템 DB 저장에 실패하면 try-catch로 파일을 삭제하지만,
 * 서버 크래시나 OOM 발생 시 삭제 로직이 실행되지 않아 디스크에 고아 파일이 영구 잔류한다.
 * </p>
 *
 * <p>
 * 해결: 매일 새벽 3시에 업로드 디렉토리를 스캔하고, DB의 item.image에 참조되지 않으면서
 * 생성 후 24시간이 지난 파일을 일괄 삭제한다.
 * </p>
 */
@Component
@Slf4j
public class OrphanImageCleanupScheduler {

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;
    private final Path uploadDirPath;

    // 고아 파일로 판정하기 위한 최소 경과 시간 (시간 단위)
    private static final long ORPHAN_THRESHOLD_HOURS = 24;

    public OrphanImageCleanupScheduler(
        ItemRepository itemRepository,
        ObjectMapper objectMapper,
        @Value("${image.storage.path:./uploads}") String uploadPath
    ) {
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
        this.uploadDirPath = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    /**
     * 매일 새벽 3시에 실행: DB에 참조되지 않는 고아 이미지 파일을 삭제한다.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanupOrphanImages() {
        log.info(
            "[OrphanImageCleanup] 고아 이미지 정리 시작 - 디렉토리: {}",
            uploadDirPath
        );

        if (!Files.isDirectory(uploadDirPath)) {
            log.warn(
                "[OrphanImageCleanup] 업로드 디렉토리가 존재하지 않음: {}",
                uploadDirPath
            );
            return;
        }

        // 1. DB에서 참조 중인 모든 파일명 수집
        Set<String> referencedFileNames = collectReferencedFileNames();
        log.info(
            "[OrphanImageCleanup] DB 참조 파일 수: {}",
            referencedFileNames.size()
        );

        // 2. 디스크 파일 스캔 및 고아 파일 삭제
        Instant threshold = Instant.now().minus(
            ORPHAN_THRESHOLD_HOURS,
            ChronoUnit.HOURS
        );
        int deletedCount = 0;
        int scannedCount = 0;

        try (Stream<Path> files = Files.list(uploadDirPath)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                if (!Files.isRegularFile(file)) continue;
                scannedCount++;

                String fileName = file.getFileName().toString();

                // DB에 참조되어 있으면 건너뛰기
                if (referencedFileNames.contains(fileName)) continue;

                // [FIX] 문제 4: creationTime()은 리눅스 파일시스템에서 신뢰성이 낮음 (lastModifiedTime과 동일하거나
                // epoch 반환 가능)
                // 안전을 위해 생성 시간과 수정 시간 중 더 최근 시각을 기준으로 24시간이 경과했는지 판단
                try {
                    BasicFileAttributes attrs = Files.readAttributes(
                        file,
                        BasicFileAttributes.class
                    );
                    Instant created = attrs.creationTime().toInstant();
                    Instant modified = attrs.lastModifiedTime().toInstant();
                    Instant referenceTime = created.isAfter(modified)
                        ? created
                        : modified;

                    if (referenceTime.isAfter(threshold)) {
                        log.debug(
                            "[OrphanImageCleanup] 최근 파일이므로 건너뜀: {}",
                            fileName
                        );
                        continue;
                    }
                } catch (IOException e) {
                    log.warn(
                        "[OrphanImageCleanup] 파일 속성 읽기 실패: {}",
                        fileName,
                        e
                    );
                    continue;
                }

                // 고아 파일 삭제
                try {
                    Files.delete(file);
                    deletedCount++;
                    log.info(
                        "[OrphanImageCleanup] 고아 파일 삭제: {}",
                        fileName
                    );
                } catch (IOException e) {
                    log.error(
                        "[OrphanImageCleanup] 파일 삭제 실패: {}",
                        fileName,
                        e
                    );
                }
            }
        } catch (IOException e) {
            log.error("[OrphanImageCleanup] 디렉토리 스캔 실패", e);
        }

        log.info(
            "[OrphanImageCleanup] 정리 완료 - 스캔: {}건, 삭제: {}건",
            scannedCount,
            deletedCount
        );
    }

    /**
     * DB의 item.image 필드에서 참조 중인 모든 파일명을 추출한다.
     * image 필드는 JSON 배열(["url1", "url2"]) 또는 단일 URL 문자열일 수 있다.
     */
    @Transactional(readOnly = true)
    private Set<String> collectReferencedFileNames() {
        Set<String> fileNames = new HashSet<>();
        try (
            java.util.stream.Stream<String> allImageUrls =
                itemRepository.findAllImageUrls()
        ) {
            allImageUrls.forEach(imageField -> {
                if (imageField != null && !imageField.isBlank()) {
                    // JSON 배열 형태인 경우
                    if (imageField.startsWith("[")) {
                        try {
                            List<String> urls = objectMapper.readValue(
                                imageField,
                                new TypeReference<List<String>>() {}
                            );
                            for (String url : urls) {
                                extractFileName(url, fileNames);
                            }
                        } catch (Exception e) {
                            // JSON 파싱 실패 시 콤마 분리 폴백 시도 (대괄호/따옴표 제거 후 split)
                            log.warn(
                                "[OrphanImageCleanup] JSON 파싱 실패, 콤마 분리 폴백 시도: {}",
                                imageField
                            );
                            String cleaned = imageField
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", "")
                                .trim();
                            if (!cleaned.isEmpty()) {
                                for (String url : cleaned.split(",")) {
                                    extractFileName(url.trim(), fileNames);
                                }
                            }
                        }
                    } else if (imageField.contains(",")) {
                        // 콤마 구분자 형태인 경우
                        for (String url : imageField.split(",")) {
                            extractFileName(url.trim(), fileNames);
                        }
                    } else {
                        // 단일 URL인 경우
                        extractFileName(imageField, fileNames);
                    }
                }
            });
        }

        return fileNames;
    }

    /**
     * URL에서 파일명만 추출하여 Set에 추가한다.
     * 예: "/api/images/uuid_file.jpg" → "uuid_file.jpg"
     */
    private void extractFileName(String url, Set<String> fileNames) {
        if (url == null || url.isBlank()) return;
        int lastSlash = url.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
        if (!fileName.isBlank()) {
            fileNames.add(fileName);
        }
    }
}
