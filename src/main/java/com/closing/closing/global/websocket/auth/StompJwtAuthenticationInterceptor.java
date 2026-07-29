package com.closing.closing.global.websocket.auth;

import com.closing.closing.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

// WebSocket의 로그인 검사
// REST에서는 요청마다 JwtAuthenticationFilter가 JWT를 검사함
@Component
@RequiredArgsConstructor
public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        // 메세지의 접근자를 가져옴
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 접근자를 불러올 수 없으면 메세지 그대로 반환
        if (accessor == null) {
            return  message;
        }

        // 접근자를 통해 토큰을 검사함
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new  IllegalArgumentException("인증 토큰이 필요합니다.");
            }

            String token = authorization.substring(7);

            jwtProvider.validate(token);

            Long userId = jwtProvider.getUserId(token);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    Collections.emptyList()
            );

            accessor.setUser(authentication);
        }

        return message;
    }


}
