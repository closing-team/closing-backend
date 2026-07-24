package com.closing.closing.domain.product.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        상품 정렬 방식:
        LATEST(최신순), POPULAR(북마크 많은 순), NEAREST(거리 가까운 순),
        CHEAPEST(낮은 가격순), MOST_EXPENSIVE(높은 가격순)
        """)
public enum SortMethod {
    LATEST, POPULAR, NEAREST, CHEAPEST, MOST_EXPENSIVE
}
