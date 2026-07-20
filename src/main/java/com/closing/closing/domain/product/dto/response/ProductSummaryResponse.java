package com.closing.closing.domain.product.dto.response;

import com.closing.closing.domain.product.entity.TradeMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ProductSummaryResponse {

    private final Long productId;
    private final String thumbnailUrl;
    private final String title;
    private final Long price;
    private final List<TradeMethod> tradeMethods;
    private final TradeLocation

}
