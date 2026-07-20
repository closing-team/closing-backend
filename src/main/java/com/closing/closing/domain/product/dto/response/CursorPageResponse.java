package com.closing.closing.domain.product.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CursorPageResponse<C> {

    private final C nextCursor;
    private final boolean hasNext;

    public static <C> CursorPageResponse<C> of(
            C nextCursor,
            boolean hasNext
    ) {
        return new CursorPageResponse<C>(nextCursor, hasNext);
    }
}
