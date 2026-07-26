package com.closing.closing.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = "커서 기반 상품 목록 응답")
@RequiredArgsConstructor
@Getter
// T: 목록이 될 상품의 타입
// C: 커서의 타입
public class ProductListResponse<T, C> {

    @Schema(description = "조회된 상품 목록")
    private final List<T> products;

    @Schema(description = "다음 페이지 조회 정보")
    private final CursorPageResponse<C> page;

}
