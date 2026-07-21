package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.closing.closing.domain.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCreateRequest {

    @NotBlank(message = "상품 제목은 필수입니다.")
    private String title;

    @NotNull(message = "업종 카테고리는 필수입니다.")
    private BusinessCategory businessCategory;

    @NotNull(message = "품목 카테고리는 필수입니다.")
    private ProductCategory productCategory;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    private Integer price;

    @NotEmpty(message = "거래 방법을 하나 이상 선택해야 합니다.")
    private List<@NotNull TradeMethod> tradeMethods;

    // 직거래 시 필수, 직거래 아닌 경우 null 가능
    private String tradeLocation;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    // 직거래일 경우 프론트가 지도에서 선택한 위치
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;

    public Product toEntity(User seller, List<String> imageUrls) {
        boolean isDirectAvailable = tradeMethods.contains(TradeMethod.DIRECT);
        boolean isDeliveryAvailable = tradeMethods.contains(TradeMethod.DELIVERY);

        return Product.builder()
                .seller(seller)
                .title(title)
                .businessCategory(businessCategory)
                .productCategory(productCategory)
                .price(price)
                .description(description)
                .imageUrls(imageUrls)
                .isDirectAvailable(isDirectAvailable)
                .isDeliveryAvailable(isDeliveryAvailable)
                .tradeLocation(isDirectAvailable ? tradeLocation : null)
                .latitude(isDirectAvailable ? latitude : null)
                .longitude(isDirectAvailable ? longitude : null)
                .build();
    }
}
