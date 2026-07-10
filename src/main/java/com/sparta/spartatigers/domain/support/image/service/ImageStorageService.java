package com.sparta.spartatigers.domain.support.image.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageStorageService {
    List<String> uploadImages(List<MultipartFile> images);

    void deleteImages(List<String> imageUrls);
}
