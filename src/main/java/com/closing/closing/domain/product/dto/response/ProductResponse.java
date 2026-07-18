package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class ProductResponse {

    private final Long productId;
    private final String title;
    private final int price;
    private final String description;
    private final List<String> imageUrls;
    private final BusinessCategory businessCategory;
    private final String businessCategoryName;
    private final ProductCategory productCategory;
    private final String productCategoryName;
    private final List<TradeMethod> tradeMethods;
    private final String tradeLocation;
    private final ProductStatus status;
    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;
    @JsonProperty("isOwner")
    private final boolean isOwner;
    private final SellerResponse seller;
    private final LocalDateTime createdAt;

    // Product 객체로부터 ProductResponse를 만드는 함수
    public static ProductResponse from(Product product, List<TradeMethod> tradeMethods, boolean isBookmarked, boolean isOwner) {
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
                product.getTradeLocation(),
                product.getStatus(),
                isBookmarked,
                isOwner,
                SellerResponse.from(product.getSeller()),
                product.getCreatedAt()
        );
    }


}
