package com.closing.closing.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "상품 상태 변경 요청")
@NoArgsConstructor
@Getter
public class ProductStatusRequest {

    @Schema(
            description = "변경할 상품 상태. DELETED는 상태 변경 API에서 사용할 수 없습니다.",
            example = "RESERVED",
            allowableValues = {"SELLING", "RESERVED", "SOLD_OUT"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "상품 상태는 필수입니다.")
    private String status;

}
