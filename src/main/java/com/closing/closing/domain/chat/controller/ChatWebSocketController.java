package com.closing.closing.domain.chat.controller;

import com.closing.closing.domain.chat.dto.request.websocket.ChatTextMessageRequest;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageResult;
import com.closing.closing.domain.chat.service.ChatMessageService;
import com.closing.closing.domain.chat.service.ChatRealtimePublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatRealtimePublisher chatRealtimePublisher;

    @MessageMapping("/chat-rooms/{chatRoomId}/messages")
    public void sendTextMessage(
            @DestinationVariable Long chatRoomId,
            @Valid ChatTextMessageRequest request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        ChatMessageResult result =
                chatMessageService.sendTextMessage(
                        userId,
                        chatRoomId,
                        request.content()
                );

        chatRealtimePublisher.publish(result);
    }
}
