package com.closing.closing.domain.chat.dto.response.websocket;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.MessageType;

import java.time.LocalDateTime;

public record ChatMessageEvent (

    Long chatRoomId,

    Long messageId,

    Long senderId,

    MessageType messageType,

    String content,

    boolean read,

    LocalDateTime createdAt

) {
    public static ChatMessageEvent from(
            Long chatRoomId,
            ChatMessage message
    ) {
        return new ChatMessageEvent(
                chatRoomId,
                message.getId(),
                message.getSender().getId(),
                message.getMessageType(),
                message.getContent(),
                message.isRead(),
                message.getCreatedAt()
        );
    }
}
