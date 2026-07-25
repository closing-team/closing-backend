package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = "채팅방 목록 조회 응답")
@Getter
@RequiredArgsConstructor
public class ChatRoomListResponse<C> {

    @Schema(description = "최근 메시지 순으로 정렬된 채팅방 목록")
    private final List<ChatRoomResponse> chatRooms;

    @Schema(description = "다음 페이지 조회 정보")
    private final CursorPageResponse<C> page;
}
