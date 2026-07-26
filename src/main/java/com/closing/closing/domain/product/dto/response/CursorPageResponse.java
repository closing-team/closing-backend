package com.closing.closing.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "커서 기반 페이지 정보")
@RequiredArgsConstructor
@Getter
public class CursorPageResponse<C> {

    @Schema(description = "다음 페이지 조회에 사용할 커서. 다음 페이지가 없으면 null입니다.")
    private final C nextCursor;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    public static <C> CursorPageResponse<C> of(
            C nextCursor,
            boolean hasNext
    ) {
        return new CursorPageResponse<C>(nextCursor, hasNext);
    }
}
