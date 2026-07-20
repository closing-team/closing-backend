package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class ProductStatusResponse {

    private final Long productId;
    private final ProductStatus status;
    private final LocalDateTime updatedAt;

    public static ProductStatusResponse from(Product product) {

        // ProductStatusResponse에 들어가는 status는 바뀐 값이므로 먼저 변경하고 Response를 만들어야 함
        return new ProductStatusResponse(product.getId(), product.getStatus(), product.getUpdatedAt());
    }
}
