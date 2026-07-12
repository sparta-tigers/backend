package com.sparta.spartatigers.domain.support.image.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    List<String> uploadImages(List<MultipartFile> images);

    void deleteImages(List<String> imageUrls);
}
