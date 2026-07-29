package com.closing.closing.global.websocket.auth;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class StompDestinationAuthorizationInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_MESSAGE_SEND_DESTINATION =
            Pattern.compile(
                    "^/app/chat-rooms/\\d+/messages$"
            );

    private static final String CHAT_MESSAGE_SUBSCRIPTION = "/user/queue/chat-messages";

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)
                || StompCommand.DISCONNECT.equals(command)
                || StompCommand.UNSUBSCRIBE.equals(command)) {
            return message;
        }

        if (accessor.getUser() == null) {
            throw new AccessDeniedException(
                    "인증되지 않은 WebSocket 요청입니다."
            );
        }

        String destination = accessor.getDestination();

        if (StompCommand.SEND.equals(command)) {
            validateSendDestination(destination);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscribeDestination(destination);
            return message;
        }

        throw new AccessDeniedException(
                "허용되지 않은 STOMP 명령입니다."
        );
    }

    private void validateSendDestination(String destination) {
        boolean allowed = destination != null
                && CHAT_MESSAGE_SEND_DESTINATION
                .matcher(destination).matches();

        if (!allowed) {
            throw new AccessDeniedException(
                    "허용되지 않은 메시지 전송 주소입니다."
            );
        }
    }

    private void validateSubscribeDestination(String destination) {
        if (!CHAT_MESSAGE_SUBSCRIPTION.equals(destination)) {
            throw new AccessDeniedException(
                    "허용되지 않은 메시지 구독 주소입니다."
            );
        }
    }
}
