package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "상품 등록 응답")
@Getter
@RequiredArgsConstructor
public class ProductCreateResponse {

    @Schema(description = "등록된 상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "등록된 상품 상태", example = "SELLING")
    private final ProductStatus status;

    @Schema(description = "S3에 저장된 상품 이미지 URL 목록")
    private final List<String> imageUrls;

    @Schema(description = "상품 등록 일시", example = "2026-07-24T14:30:00")
    private final LocalDateTime createdAt;

    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(
                product.getId(),
                product.getStatus(),
                product.getImageUrls(),
                product.getCreatedAt()
        );
    }
}
