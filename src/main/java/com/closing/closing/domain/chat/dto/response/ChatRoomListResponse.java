package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChatRoomListResponse<C> {

    private final List<ChatRoomResponse> chatRooms;
    private final CursorPageResponse<C> page;
}
