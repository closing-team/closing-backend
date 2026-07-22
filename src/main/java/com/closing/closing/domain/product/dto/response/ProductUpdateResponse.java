package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ProductUpdateResponse {

    private final Long productId;
    private final String title;
    private final Integer price;
    private final List<TradeMethod> tradeMethods;
    private final String tradeLocation;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final ProductStatus status;
    private final List<String> imageUrls;
    private final LocalDateTime updatedAt;

    public static ProductUpdateResponse from(Product product) {

        return new ProductUpdateResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getTradeMethods(),
                product.getTradeLocation(),
                product.getLatitude(),
                product.getLongitude(),
                product.getStatus(),
                product.getImageUrls(),
                product.getUpdatedAt()
        );
    }

}
