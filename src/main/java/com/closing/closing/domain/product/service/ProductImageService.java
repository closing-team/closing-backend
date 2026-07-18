package com.closing.closing.domain.product.service;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private static final int MAX_IMAGE_COUNT = 10;
    private static final String PRODUCT_DIRECTORY = "products";

    /*
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of (
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    */

    private final ImageStorage imageStorage;

    public List<String> upload(List<MultipartFile> images) {
        validateImages(images);

        List<String> uploadedImageUrls = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String imageUrl =
                        imageStorage.upload(image, PRODUCT_DIRECTORY);

                uploadedImageUrls.add(imageUrl);
            }

            return uploadedImageUrls;
        } catch (RuntimeException exception) {
            deleteUploadedImages(uploadedImageUrls);
            throw exception;
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null
                || images.isEmpty()
                || images.size() > MAX_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_COUNT);
        }
        /*
        for (MultipartFile image : images) {
            if (image.isEmpty()
                    || !ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
        }
        */
    }

    private void deleteUploadedImages(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                imageStorage.delete(imageUrl);
            } catch (RuntimeException exception) {
                log.warn("이미지 정리 중 삭제에 실패했습니다. imageUrl={}", imageUrl, exception);
            }
        }
    }
}
