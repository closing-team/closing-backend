package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 상품 목록의 상품 요약 정보")
@Getter
@RequiredArgsConstructor
public class MyProductSummaryResponse {

    @Schema(description = "상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "대표 이미지 URL. 등록된 이미지가 없으면 null입니다.", example = "https://example.com/products/15/main.jpg")
    private final String thumbnailUrl;

    @Schema(description = "상품 제목", example = "업소용 냉장고")
    private final String title;

    @Schema(description = "상품 가격(원)", example = "350000")
    private final Integer price;

    @Schema(description = "지원하는 거래 방식 목록")
    private final List<TradeMethod> tradeMethods;

    @Schema(description = "직거래 정보. 직거래를 지원하지 않으면 null이며, 내 상품 조회에서는 distanceKm이 null입니다.")
    private final TradeLocationResponse tradeLocation;

    @Schema(description = "상품 상태", example = "SELLING")
    private final ProductStatus status;

    @Schema(description = "현재 사용자의 북마크 여부", example = "false")
    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    @Schema(description = "해당 상품의 전체 북마크 수", example = "8")
    private final long bookmarkCount;

    @Schema(description = "상품 등록 일시", example = "2026-07-24T14:30:00")
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
