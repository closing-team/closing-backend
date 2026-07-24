package com.closing.closing.domain.product.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
// T: 목록이 될 상품의 타입
// C: 커서의 타입
public class ProductListResponse<T, C> {

    private final List<T> products;
    private final CursorPageResponse<C> page;

}
