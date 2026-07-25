package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "상품 북마크 추가·삭제 응답")
@Getter
@RequiredArgsConstructor
public class ProductBookmarkResponse {

    @Schema(description = "북마크 상태를 변경한 상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "변경 후 현재 사용자의 북마크 여부", example = "true")
    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    public static ProductBookmarkResponse from(Product product, boolean isBookmarked) {
        return new ProductBookmarkResponse(
                product.getId(),
                isBookmarked
        );
    }
}
