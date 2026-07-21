package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductBookmarkResponse {
    private final Long productId;

    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    public static ProductBookmarkResponse from(Product product, boolean isBookmarked) {
        return new ProductBookmarkResponse(
                product.getId(),
                isBookmarked
        );
    }
}
