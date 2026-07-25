package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.entity.TradeMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "상품 수정 응답")
@Getter
@RequiredArgsConstructor
public class ProductUpdateResponse {

    @Schema(description = "수정된 상품 ID", example = "15")
    private final Long productId;

    @Schema(description = "수정 후 상품 제목", example = "업소용 냉장고")
    private final String title;

    @Schema(description = "수정 후 상품 가격(원)", example = "300000")
    private final Integer price;

    @Schema(description = "수정 후 지원하는 거래 방식 목록")
    private final List<TradeMethod> tradeMethods;

    @Schema(description = "수정 후 직거래 장소. 직거래를 지원하지 않으면 null입니다.", example = "서울특별시 중구 명동")
    private final String tradeLocation;

    @Schema(description = "수정 후 직거래 장소의 위도. 직거래를 지원하지 않으면 null입니다.", example = "37.5665")
    private final BigDecimal latitude;

    @Schema(description = "수정 후 직거래 장소의 경도. 직거래를 지원하지 않으면 null입니다.", example = "126.9780")
    private final BigDecimal longitude;

    @Schema(description = "상품 상태", example = "SELLING")
    private final ProductStatus status;

    @Schema(description = "수정 후 상품 이미지 URL 목록")
    private final List<String> imageUrls;

    @Schema(description = "상품 수정 일시", example = "2026-07-24T15:00:00")
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
