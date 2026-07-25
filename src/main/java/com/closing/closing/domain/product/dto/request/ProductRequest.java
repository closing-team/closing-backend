package com.closing.closing.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "상품 상세 조회 시 거리 계산에 사용할 현재 위치")
@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {

    @Schema(
            description = "사용자 현재 위치의 위도. 경도와 함께 전달하거나 둘 다 생략합니다.",
            example = "37.5665",
            minimum = "-90",
            maximum = "90"
    )
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @Schema(
            description = "사용자 현재 위치의 경도. 위도와 함께 전달하거나 둘 다 생략합니다.",
            example = "126.9780",
            minimum = "-180",
            maximum = "180"
    )
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;

    @Schema(hidden = true)
    @AssertTrue(message = "위도와 경도는 함께 전달해야 합니다.")
    public boolean isLocationPairValid() {
        return (latitude == null && longitude == null)
                || (latitude != null && longitude != null);
    }
}
