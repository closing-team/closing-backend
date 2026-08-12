package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.TradeMethod;
import com.closing.closing.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 등록 정보. multipart/form-data의 request 파트에 JSON 형식으로 전달합니다.")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCreateRequest {

    @Schema(
            description = "상품 제목",
            example = "업소용 냉장고",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품 제목은 필수입니다.")
    private String title;

    @Schema(
            description = "업종별 카테고리",
            example = "KOREAN_MEAL",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "업종 카테고리는 필수입니다.")
    private BusinessCategory businessCategory;

    @Schema(
            description = "품목별 카테고리",
            example = "REFRIGERATOR_FREEZER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "품목 카테고리는 필수입니다.")
    private ProductCategory productCategory;

    @Schema(
            description = "상품 가격(원). 0원은 나눔 상품입니다.",
            example = "350000",
            minimum = "0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Schema(
            description = "지원하는 거래 방식 목록. 하나 이상 전달합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "거래 방법을 하나 이상 선택해야 합니다.")
    private List<@NotNull TradeMethod> tradeMethods;

    @Schema(
            description = "직거래 장소의 주소 또는 지역명. tradeMethods에 DIRECT가 포함된 경우 필수입니다.",
            example = "서울특별시 중구 명동"
    )
    private String tradeLocation;

    @Schema(
            description = "상품 상세 설명",
            example = "2년 사용한 업소용 냉장고입니다.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @Schema(
            description = "직거래 장소의 위도. tradeMethods에 DIRECT가 포함된 경우 필수입니다.",
            example = "37.5665",
            minimum = "-90",
            maximum = "90"
    )
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @Schema(
            description = "직거래 장소의 경도. tradeMethods에 DIRECT가 포함된 경우 필수입니다.",
            example = "126.9780",
            minimum = "-180",
            maximum = "180"
    )
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
