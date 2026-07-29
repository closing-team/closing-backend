package com.closing.closing.global.websocket.config;


import com.closing.closing.global.websocket.auth.StompDestinationAuthorizationInterceptor;
import com.closing.closing.global.websocket.auth.StompJwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


// WebSocket 전체의 주소 체계를 정함
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtAuthenticationInterceptor authenticationInterceptor;
    private final StompDestinationAuthorizationInterceptor destinationInterceptor;

    // /ws-chat: WebSocket 연결 시작 주소
    // ex) ws://localhost:8080/ws-chat

    // origin 설정
    // .setAllowedOrigins("*")로 모든 주소를 열어두면 보안상 위험
    // 실제 프론트 주소 지정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("http://localhost:3000");
    }

    // /app: 클라이언트가 서버로 메세지를 보내는 주소
    // ex) /app/chat-rooms/12/messages
    // /app로 시작하는 메세지는 Spring의 @MessageMapping으로 전달

    // /user/queue: 서버가 특정 사용자에게 메세지를 전달하는 주소
    // /user 목적지를 로그인한 사용자별 개인 큐로 반환
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/queue");
    }

    // 클라이언트에서 들어오는 STOMP 메시지에 인터셉터 두 개 적용
    // 1. jwt 인증 인터셉터
    // 2. destination 인터셉터 - 허용된 주소로 요청이 들어왔는지
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor,  destinationInterceptor);
    }
}
