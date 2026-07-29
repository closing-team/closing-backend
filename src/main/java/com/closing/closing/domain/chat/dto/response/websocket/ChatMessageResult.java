package com.closing.closing.domain.chat.dto.response.websocket;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;

import java.util.List;

public record ChatMessageResult(
        Long buyerId,
        Long sellerId,
        List<ChatMessageEvent> events
) {

    public static ChatMessageResult from(
            ChatRoom chatRoom,
            ChatMessage message
    ) {
        return from(
                chatRoom,
                List.of(message)
        );
    }

    public static ChatMessageResult from(
            ChatRoom chatRoom,
            List<ChatMessage> messages
    ) {
        List<ChatMessageEvent> events =
                messages.stream()
                        .map(message ->
                                ChatMessageEvent.from(
                                        chatRoom.getId(),
                                        message
                                )
                        ).toList();

        return new ChatMessageResult(
                chatRoom.getBuyer().getId(),
                chatRoom.getSeller().getId(),
                events
        );
    }
}
