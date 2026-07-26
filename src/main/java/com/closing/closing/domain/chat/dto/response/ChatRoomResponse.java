package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "채팅방 목록의 채팅방 요약 정보")
@Getter
@RequiredArgsConstructor
public class ChatRoomResponse {

    @Schema(description = "채팅방 ID", example = "12")
    private final Long chatRoomId;

    @Schema(description = "현재 사용자를 기준으로 한 채팅 상대방 정보")
    private final ChatRoomOtherMemberResponse otherMember;

    @Schema(description = "문의 대상 상품 정보")
    private final ChatRoomProductResponse product;

    @Schema(description = "가장 최근 메시지 내용. 이미지 메시지이면 이미지 URL입니다.", example = "아직 판매 중인가요?")
    private final String lastMessage;

    @Schema(description = "가장 최근 메시지 전송 일시", example = "2026-07-24T15:10:00")
    private final LocalDateTime lastMessageAt;

    @Schema(description = "상대방이 보내고 현재 사용자가 아직 읽지 않은 메시지 개수", example = "3")
    private final Integer unReadMessagesCount;

    public static ChatRoomResponse from(
            ChatRoom chatRoom,
            User user,
            ChatMessage lastMessage,
            Integer unReadMessagesCount
    ) {
        User otherMember = chatRoom.getBuyer().getId().equals(user.getId())
                ? chatRoom.getSeller()
                : chatRoom.getBuyer();

        return new ChatRoomResponse(
                chatRoom.getId(),
                ChatRoomOtherMemberResponse.from(otherMember),
                ChatRoomProductResponse.from(chatRoom.getProduct()),
                lastMessage.getContent(),
                lastMessage.getCreatedAt(),
                unReadMessagesCount
        );
    }
}
