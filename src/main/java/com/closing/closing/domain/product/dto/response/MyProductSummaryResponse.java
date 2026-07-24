package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MyProductSummaryResponse {

    private final Long productId;
    private final String thumbnailUrl;
    private final String title;
    private final Integer price;
    private final List<TradeMethod> tradeMethods;
    private final TradeLocationResponse tradeLocation;
    private final ProductStatus status;

    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    private final long bookmarkCount;
    private final LocalDateTime createdAt;

    public static MyProductSummaryResponse from(
            Product product,
            boolean isBookmarked,
            long bookmarkCount
    ) {
        List<String> imageUrls = product.getImageUrls();

        String thumbnailUrl =
                imageUrls == null || imageUrls.isEmpty()
                        ? null
                        : imageUrls.get(0);

        TradeLocationResponse tradeLocation =
                product.isDirectAvailable()
                        ? TradeLocationResponse.of(
                        product.getTradeLocation(),
                        null
                )
                        : null;

        return new MyProductSummaryResponse(
                product.getId(),
                thumbnailUrl,
                product.getTitle(),
                product.getPrice(),
                product.getTradeMethods(),
                tradeLocation,
                product.getStatus(),
                isBookmarked,
                bookmarkCount,
                product.getCreatedAt()
        );
    }

}
