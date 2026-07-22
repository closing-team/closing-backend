package com.closing.closing.domain.product.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TradeLocationResponse {

    private final String district;
    private final Double distanceKm;

    public static TradeLocationResponse of(
            String district,
            Double distanceKm
    ) {
        return new TradeLocationResponse(
                district,
                distanceKm
        );
    }
}
