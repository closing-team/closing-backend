package com.closing.closing.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    String upload(MultipartFile image, String directory);

    void delete(String imageUrl);
}
