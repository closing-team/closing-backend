package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MessageResponse {

    private final Long messageId;
    private final Long senderId;
    private final MessageType messageType;
    private final String content;
    private final boolean mine;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public static MessageResponse from(
            ChatMessage chatMessage,
            Long userId
    ) {
        return new MessageResponse(
                chatMessage.getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.getSender().getId().equals(userId),
                chatMessage.isRead(),
                chatMessage.getCreatedAt()
        );
    }

}
