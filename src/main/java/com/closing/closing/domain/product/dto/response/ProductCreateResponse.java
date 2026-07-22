package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ProductCreateResponse {

    private final Long productId;
    private final ProductStatus status;
    private final List<String> imageUrls;
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
