package com.closing.closing.domain.product.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MyProductCountsResponse {

    private final long total;
    private final long selling;
    private final long reserved;
    private final long soldOut;

}
