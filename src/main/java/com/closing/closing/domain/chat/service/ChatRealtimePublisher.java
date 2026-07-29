package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.response.MessageResponse;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageEvent;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// DB에 저장된 메시지를 실제 WebSocket으로 보냄
@Component
@RequiredArgsConstructor
public class ChatRealtimePublisher {

    private static final String CHAT_MESSAGE_DESTINATION =
            "/queue/chat-messages";

    // 서버에서 STOMP 메시지를 발행할 때 사용하는 Spring 도구
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(ChatMessageResult result) {
        for (ChatMessageEvent event : result.events()) {
            sendToUser(
                    result.buyerId(),
                    event
            );

            sendToUser(
                    result.sellerId(),
                    event
            );
        }
    }

    private void sendToUser(Long userId, ChatMessageEvent event) {
        MessageResponse response = MessageResponse.from(event, userId);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                CHAT_MESSAGE_DESTINATION,
                response
        );
    }
}