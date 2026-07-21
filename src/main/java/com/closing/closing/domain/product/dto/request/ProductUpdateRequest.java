package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.TradeMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "상품 제목은 필수입니다.")
    private String title;

    @NotNull(message = "업종 카테고리는 필수입니다.")
    private BusinessCategory businessCategory;

    @NotNull(message = "상품 카테고리는 필수입니다.")
    private ProductCategory productCategory;

    @NotNull(message = "상품 가격은 필수입니다.")
    @Positive(message = "상품 가격은 양수여야 합니다.")
    private Integer price;

    @NotEmpty(message = "거래 방식은 필수입니다.")
    private List<TradeMethod> tradeMethods;

    private String tradeLocation;

    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;

    @AssertTrue(message = "위도와 경도는 함께 전달해야 합니다.")
    public boolean isLocationPairValid() {
        return (latitude == null && longitude == null)
                || (latitude != null && longitude != null);
    }

    @NotBlank(message = "상세 내용은 필수입니다.")
    private String description;

    @NotNull(message = "유지할 기존 이미지 목록은 필수입니다.")
    private List<String> retainedImages;


}