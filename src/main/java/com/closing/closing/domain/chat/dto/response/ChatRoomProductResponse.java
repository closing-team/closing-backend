package com.closing.closing.domain.chat.dto.response;

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

}
