package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MessageSendResponse {

    private final List<MessageResponse> messages;

    public static MessageSendResponse from(
            List<ChatMessage> chatMessages,
            Long userId
    ) {
        List<MessageResponse> messages =
                chatMessages.stream()
                        .map(chatMessage ->
                                MessageResponse.from(
                                        chatMessage,
                                        userId
                                )
                        )
                        .toList();

        return new MessageSendResponse(messages);
    }

}
