package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatRoomProductResponse {

    private final Long productId;
    private final String title;
    private final String thumbnailUrl;
    private final Integer price;
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
