package com.closing.closing.domain.product.dto.request;

import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.entity.TradeMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 수정 정보. 변경하지 않는 값도 현재 값으로 보내는 전체 수정 요청입니다.")
@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest {

    @Schema(
            description = "수정 후 상품 제목",
            example = "업소용 냉장고",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품 제목은 필수입니다.")
    private String title;

    @Schema(
            description = "수정 후 업종별 카테고리",
            example = "KOREAN_MEAL",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "업종 카테고리는 필수입니다.")
    private BusinessCategory businessCategory;

    @Schema(
            description = "수정 후 품목별 카테고리",
            example = "REFRIGERATOR_FREEZER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 카테고리는 필수입니다.")
    private ProductCategory productCategory;

    @Schema(
            description = "수정 후 상품 가격(원)",
            example = "300000",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 가격은 필수입니다.")
    @Positive(message = "상품 가격은 양수여야 합니다.")
    private Integer price;

    @Schema(
            description = "수정 후 지원하는 거래 방식 목록. 하나 이상 전달합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "거래 방식은 필수입니다.")
    private List<@NotNull TradeMethod> tradeMethods;

    @Schema(
            description = "직거래 장소의 주소 또는 지역명. tradeMethods에 DIRECT가 포함된 경우 필수입니다.",
            example = "서울특별시 중구 명동"
    )
    private String tradeLocation;

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

    @Schema(hidden = true)
    @AssertTrue(message = "위도와 경도는 함께 전달해야 합니다.")
    public boolean isLocationPairValid() {
        return (latitude == null && longitude == null)
                || (latitude != null && longitude != null);
    }

    @Schema(
            description = "수정 후 상품 상세 설명",
            example = "가격을 낮췄습니다. 정상 작동합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상세 내용은 필수입니다.")
    private String description;

    @Schema(
            description = """
                    수정 후에도 유지할 기존 S3 이미지 URL 목록입니다.
                    기존 이미지 중 이 목록에 없는 이미지는 삭제됩니다.
                    새 이미지 파일은 multipart/form-data의 newImages 파트로 별도 전달합니다.
                    """,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "유지할 기존 이미지 목록은 필수입니다.")
    private List<String> retainedImages;


}
