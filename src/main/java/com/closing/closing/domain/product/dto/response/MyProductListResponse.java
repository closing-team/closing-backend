package com.closing.closing.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = "내 상품 목록 조회 응답")
@Getter
@RequiredArgsConstructor
public class MyProductListResponse {

    @Schema(description = "조회된 내 상품 목록")
    private final List<MyProductSummaryResponse> products;

    @Schema(description = "필터와 관계없이 계산한 내 상품 상태별 개수")
    private final MyProductCountsResponse counts;

    @Schema(description = "다음 페이지 조회 정보")
    private final CursorPageResponse<Long> page;
}
