package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "상품 상세 조회 응답")
@RequiredArgsConstructor
@Getter
public class ProductResponse {

    @Schema(description = "상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "상품 제목", example = "업소용 냉장고")
    private final String title;

    @Schema(description = "상품 가격(원)", example = "350000")
    private final int price;

    @Schema(description = "상품 상세 설명", example = "2년 사용한 업소용 냉장고입니다.")
    private final String description;

    @Schema(description = "상품 이미지 URL 목록")
    private final List<String> imageUrls;

    @Schema(description = "업종별 카테고리 코드", example = "KOREAN_MEAL")
    private final BusinessCategory businessCategory;

    @Schema(description = "업종별 카테고리 한글 표시명", example = "한식/백반")
    private final String businessCategoryName;

    @Schema(description = "품목별 카테고리 코드", example = "REFRIGERATOR_FREEZER")
    private final ProductCategory productCategory;

    @Schema(description = "품목별 카테고리 한글 표시명", example = "냉장고/냉동고")
    private final String productCategoryName;

    @Schema(description = "지원하는 거래 방식 목록")
    private final List<TradeMethod> tradeMethods;

    @Schema(description = "직거래 정보. 직거래를 지원하지 않으면 null입니다.")
    private final TradeLocationResponse tradeLocation;

    @Schema(description = "상품 상태", example = "SELLING")
    private final ProductStatus status;

    @Schema(description = "현재 사용자의 북마크 여부", example = "true")
    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    @Schema(description = "현재 사용자가 상품 판매자인지 여부", example = "false")
    @JsonProperty("isOwner")
    private final boolean isOwner;

    @Schema(description = "상품 판매자 정보")
    private final SellerResponse seller;

    @Schema(description = "상품 등록 일시", example = "2026-07-24T14:30:00")
    private final LocalDateTime createdAt;

    // Product 객체로부터 ProductResponse를 만드는 함수
    public static ProductResponse from(
            Product product,
            List<TradeMethod> tradeMethods,
            boolean isBookmarked,
            boolean isOwner,
            Double distanceKm
    ) {
        TradeLocationResponse tradeLocation = product.isDirectAvailable()
                ? TradeLocationResponse.of(product.getTradeLocation(), distanceKm)
                : null;

        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrls(),
                product.getBusinessCategory(),
                product.getBusinessCategory().getDisplayName(),
                product.getProductCategory(),
                product.getProductCategory().getDisplayName(),
                tradeMethods,
                tradeLocation,
                product.getStatus(),
                isBookmarked,
                isOwner,
                SellerResponse.from(product.getSeller()),
                product.getCreatedAt()
        );
    }


}
