package com.closing.closing.domain.product.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래 방식: DIRECT(직거래), DELIVERY(택배거래)")
public enum TradeMethod {
    DIRECT, DELIVERY
}
