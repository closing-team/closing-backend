package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class ProductSummaryResponse {

    private final Long productId;
    private final String thumbnailUrl;
    private final String title;
    private final Integer price;
    private final List<TradeMethod> tradeMethods;
    private final String tradeLocation;
    private final ProductStatus status;
    private final LocalDateTime createdAt;

    public static ProductSummaryResponse from(Product product) {

        List<String> imageUrls = product.getImageUrls();
        String thumbnailUrl = imageUrls == null || imageUrls.isEmpty() ? null : imageUrls.get(0);

        return new ProductSummaryResponse(
                product.getId(),
                thumbnailUrl,
                product.getTitle(),
                product.getPrice(),
                product.getTradeMethods(),
                product.getTradeLocation(),
                product.getStatus(),
                product.getCreatedAt()
        );
    }

}
