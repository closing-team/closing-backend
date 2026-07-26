package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "채팅방의 문의 대상 상품 정보")
@Getter
@RequiredArgsConstructor
public class ChatRoomProductResponse {

    @Schema(description = "상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "상품 제목", example = "업소용 냉장고")
    private final String title;

    @Schema(description = "상품 대표 이미지 URL", example = "https://example.com/products/15/main.jpg")
    private final String thumbnailUrl;

    @Schema(description = "상품 가격(원)", example = "350000")
    private final Integer price;

    @Schema(description = "상품 상태", example = "SELLING")
    private final ProductStatus status;

    public static ChatRoomProductResponse from(Product product) {

        return new ChatRoomProductResponse(
                product.getId(),
                product.getTitle(),
                product.getImageUrls().get(0),
                product.getPrice(),
                product.getStatus()
        );
    }
}
