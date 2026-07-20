package com.closing.closing.domain.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductBookmarkListRequest {

    @Positive(message = "커서는 양수여야 합니다.")
    private Long cursor;

    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.") // 임시 정책
    private Integer size = 20;

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

}
