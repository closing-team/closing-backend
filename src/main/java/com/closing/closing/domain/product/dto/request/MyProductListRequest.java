package com.closing.closing.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "내 상품 목록 조회 조건")
@Getter
@Setter
@NoArgsConstructor
public class MyProductListRequest {

    @Schema(
            description = "상품 상태 필터. 생략하면 삭제된 상품을 제외한 전체 상태를 조회합니다.",
            example = "SELLING",
            allowableValues = {"SELLING", "RESERVED", "SOLD_OUT"}
    )
    private String status;

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

}
