package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MessageHistoryListResponse<C> {

    private final List<MessageResponse> messages;
    private final CursorPageResponse<C> page;
}
