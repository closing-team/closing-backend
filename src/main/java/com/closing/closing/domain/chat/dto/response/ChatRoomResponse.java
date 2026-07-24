package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChatRoomResponse {

    private final Long chatRoomId;
    private final ChatRoomOtherMemberResponse otherMember;
    private final ChatRoomProductResponse product;
    private final String lastMessage;
    private final LocalDateTime lastMessageAt;
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
