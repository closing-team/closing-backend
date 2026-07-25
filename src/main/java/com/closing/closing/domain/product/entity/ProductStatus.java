package com.closing.closing.domain.product.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        상품 상태:
        SELLING(판매 중), RESERVED(예약 중), SOLD_OUT(판매 완료), DELETED(삭제됨).
        상태 변경 요청에서는 DELETED를 사용할 수 없습니다.
        """)
public enum ProductStatus {
    SELLING, SOLD_OUT, DELETED, RESERVED
}
