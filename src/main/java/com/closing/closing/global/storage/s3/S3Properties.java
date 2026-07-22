package com.closing.closing.global.storage.s3;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.s3")
public record S3Properties(
        @NotBlank String bucket,
        @NotBlank String region
) {

}
