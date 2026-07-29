package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageEvent;
import com.closing.closing.domain.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Schema(description = "채팅 메시지 전송 응답")
@Getter
@RequiredArgsConstructor
public class MessageSendResponse {

    @Schema(description = "생성된 메시지 목록. 이미지 여러 장 전송 시 이미지마다 메시지 한 개가 생성됩니다.")
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

    public static MessageSendResponse fromEvents(
            List<ChatMessageEvent> events,
            Long userId
    ) {
        List<MessageResponse> messages =
                events.stream()
                        .map(event ->
                                MessageResponse.from(
                                        event,
                                        userId
                                )
                        )
                        .toList();

        return new MessageSendResponse(messages);
    }

}
