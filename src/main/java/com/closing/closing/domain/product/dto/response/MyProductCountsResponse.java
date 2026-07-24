package com.closing.closing.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "내 상품 상태별 개수")
@Getter
@RequiredArgsConstructor
public class MyProductCountsResponse {

    @Schema(description = "삭제된 상품을 제외한 내 상품 전체 개수", example = "12")
    private final long total;

    @Schema(description = "판매 중인 내 상품 개수", example = "5")
    private final long selling;

    @Schema(description = "예약 중인 내 상품 개수", example = "2")
    private final long reserved;

    @Schema(description = "판매 완료된 내 상품 개수", example = "5")
    private final long soldOut;

}
