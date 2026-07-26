package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "상품 상태 변경 응답")
@RequiredArgsConstructor
@Getter
public class ProductStatusResponse {

    @Schema(description = "상태를 변경한 상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "변경 후 상품 상태", example = "RESERVED")
    private final ProductStatus status;

    @Schema(description = "상품 상태 변경 일시", example = "2026-07-24T15:00:00")
    private final LocalDateTime updatedAt;

    public static ProductStatusResponse from(Product product) {

        // ProductStatusResponse에 들어가는 status는 바뀐 값이므로 먼저 변경하고 Response를 만들어야 함
        return new ProductStatusResponse(product.getId(), product.getStatus(), product.getUpdatedAt());
    }
}
