package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "채팅방 생성 응답. 기존 채팅방이 있으면 해당 채팅방 정보를 반환합니다.")
@Getter
@RequiredArgsConstructor
public class ChatRoomCreateResponse {

    @Schema(description = "생성되었거나 기존에 존재하는 채팅방 ID", example = "12")
    private final Long chatRoomId;

    @Schema(description = "문의 대상 상품 정보")
    private final ChatRoomProductResponse product;

    @Schema(description = "채팅 상대방인 상품 판매자 정보")
    private final ChatRoomOtherMemberResponse otherMember;

    @Schema(description = "채팅방 최초 생성 일시", example = "2026-07-24T14:30:00")
    private final LocalDateTime createdAt;

    public static ChatRoomCreateResponse from(ChatRoom chatRoom) {

        return new ChatRoomCreateResponse(
                chatRoom.getId(),
                ChatRoomProductResponse.from(chatRoom.getProduct()),
                ChatRoomOtherMemberResponse.from(chatRoom.getSeller()),
                chatRoom.getCreatedAt()
        );

    }


}
