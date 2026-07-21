package com.closing.closing.global.storage.s3;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorage implements ImageStorage {

    private final S3Client s3Client;
    private final S3Properties properties;

    @Override
    public String upload(MultipartFile image, String directory) {
        String objectKey = createObjectKey(
                directory,
                image.getOriginalFilename()
        );

        PutObjectRequest.Builder requestBuilder =
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .contentLength(image.getSize());

        if (StringUtils.hasText(image.getContentType())) {
            requestBuilder.contentType(image.getContentType());
        }

        try (InputStream inputStream = image.getInputStream()) {
            s3Client.putObject(
                    requestBuilder.build(),
                    RequestBody.fromInputStream(
                            inputStream,
                            image.getSize()
                    )
            );

            return createImageUrl(objectKey);
        } catch (IOException | S3Exception exception) {
            log.error(
                    "S3 이미지 업로드에 실패했습니다. objectKey={}",
                    objectKey,
                    exception
            );

            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String imageUrl) {
        String objectKey = extractObjectKey(imageUrl);

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build();

        try {
            s3Client.deleteObject(request);
        } catch (S3Exception exception) {
            log.error(
                    "S3 이미지 삭제에 실패했습니다. objectKey={}",
                    objectKey,
                    exception
            );

            throw new CustomException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    private String createObjectKey(
            String directory,
            String originalFilename
    ) {
        String extension =
                StringUtils.getFilenameExtension(originalFilename);

        String fileName = UUID.randomUUID().toString();

        if (StringUtils.hasText(extension)) {
            fileName += "." + extension.toLowerCase(Locale.ROOT);
        }

        return directory + "/" + fileName;
    }

    private String createImageUrl(String objectKey) {
        GetUrlRequest request =
                GetUrlRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build();

        return s3Client.utilities()
                .getUrl(request)
                .toExternalForm();
    }

    private String extractObjectKey(String imageUrl) {
        try {
            String path = URI.create(imageUrl).getPath();

            if (!StringUtils.hasText(path) || path.length() <= 1) {
                throw new IllegalArgumentException("잘못된 S3 이미지 URL입니다.");
            }

            return path.substring(1);
        } catch (IllegalArgumentException exception) {
            log.error("S3 이미지 URL 분석에 실패했습니다. imageUrl={}", imageUrl, exception);
            throw new CustomException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

}
