package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final TradeLocationResponse tradeLocation;
    private final ProductStatus status;
    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;
    private final LocalDateTime createdAt;

    // 내 상품 조회 전용 변환 메서드 - distanceKm 필요 없음
    public static ProductSummaryResponse from(Product product) {
        return from(product, null, false);
    }

    public static ProductSummaryResponse from(
            Product product,
            Double distanceKm
    ) {
        return from(product, distanceKm, false);
    }

    public static ProductSummaryResponse from(
            Product product,
            Double distanceKm,
            boolean isBookmarked
    ) {

        List<String> imageUrls = product.getImageUrls();
        String thumbnailUrl = imageUrls == null || imageUrls.isEmpty() ? null : imageUrls.get(0);

        TradeLocationResponse tradeLocation =
                product.isDirectAvailable()
                ? TradeLocationResponse.of(
                        product.getTradeLocation(),
                        distanceKm
                )
                : null;

        return new ProductSummaryResponse(
                product.getId(),
                thumbnailUrl,
                product.getTitle(),
                product.getPrice(),
                product.getTradeMethods(),
                tradeLocation,
                product.getStatus(),
                isBookmarked,
                product.getCreatedAt()
        );
    }

}
