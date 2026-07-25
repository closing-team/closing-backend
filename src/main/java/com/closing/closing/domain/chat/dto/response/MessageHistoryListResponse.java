package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = "채팅 메시지 히스토리 조회 응답")
@Getter
@RequiredArgsConstructor
public class MessageHistoryListResponse<C> {

    @Schema(description = "오래된 메시지부터 최신 메시지 순으로 정렬된 현재 페이지의 메시지 목록")
    private final List<MessageResponse> messages;

    @Schema(description = "더 오래된 메시지 페이지 조회 정보")
    private final CursorPageResponse<C> page;
}
