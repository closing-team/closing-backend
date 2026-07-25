package com.closing.closing.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "북마크한 상품 목록 조회 조건")
@Getter
@Setter
@NoArgsConstructor
public class ProductBookmarkListRequest {

    @Schema(
            description = "다음 페이지 조회용 커서. 첫 요청에서는 생략하고 직전 응답의 nextCursor를 그대로 전달합니다.",
            example = "42"
    )
    @Positive(message = "커서는 양수여야 합니다.")
    private Long cursor;

    @Schema(
            description = "한 번에 조회할 상품 개수",
            example = "20",
            defaultValue = "20",
            minimum = "1",
            maximum = "100"
    )
    @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.") // 임시 정책
    @NotNull
    private Integer size = 20;

    @Schema(
            description = "거리 계산에 사용할 사용자 현재 위치의 위도. 경도와 함께 전달하거나 둘 다 생략합니다.",
            example = "37.5665",
            minimum = "-90",
            maximum = "90"
    )
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @Schema(
            description = "거리 계산에 사용할 사용자 현재 위치의 경도. 위도와 함께 전달하거나 둘 다 생략합니다.",
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
