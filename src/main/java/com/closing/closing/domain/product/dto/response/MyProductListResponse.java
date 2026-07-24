package com.closing.closing.domain.product.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MyProductListResponse {
    private final List<MyProductSummaryResponse> products;
    private final MyProductCountsResponse counts;
    private final CursorPageResponse<Long> page;
}
