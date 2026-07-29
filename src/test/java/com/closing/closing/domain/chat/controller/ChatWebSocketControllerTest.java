package com.closing.closing.domain.chat.controller;

import com.closing.closing.domain.chat.dto.request.websocket.ChatTextMessageRequest;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageEvent;
import com.closing.closing.domain.chat.dto.response.websocket.ChatMessageResult;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.chat.service.ChatMessageService;
import com.closing.closing.global.jwt.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatWebSocketControllerTest {

    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long CHAT_ROOM_ID = 10L;
    private static final Long MESSAGE_ID = 100L;
    private static final String CONTENT = "웹소켓 메시지";
    private static final String SUBSCRIPTION_DESTINATION =
            "/user/queue/chat-messages";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpUserRegistry userRegistry;

    private final List<StompSession> sessions = new CopyOnWriteArrayList<>();
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );
        stompClient.setMessageConverter(
                new MappingJackson2MessageConverter()
        );
    }

    @AfterEach
    void tearDown() {
        sessions.stream()
                .filter(StompSession::isConnected)
                .forEach(StompSession::disconnect);
        stompClient.stop();
    }

    @Test
    @DisplayName("텍스트 메시지를 보내면 구매자와 판매자의 개인 큐에 mine 값을 구분하여 전달한다")
    void sendTextMessage_publishesPersonalizedMessages() throws Exception {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 7, 29, 16, 30);
        ChatMessageEvent event = new ChatMessageEvent(
                CHAT_ROOM_ID,
                MESSAGE_ID,
                BUYER_ID,
                MessageType.TEXT,
                CONTENT,
                false,
                createdAt
        );
        ChatMessageResult result = new ChatMessageResult(
                BUYER_ID,
                SELLER_ID,
                List.of(event)
        );

        given(chatMessageService.sendTextMessage(
                BUYER_ID,
                CHAT_ROOM_ID,
                CONTENT
        )).willReturn(result);

        StompSession buyerSession = connect(BUYER_ID);
        StompSession sellerSession = connect(SELLER_ID);
        BlockingQueue<JsonNode> buyerMessages =
                subscribe(buyerSession, BUYER_ID);
        BlockingQueue<JsonNode> sellerMessages =
                subscribe(sellerSession, SELLER_ID);

        buyerSession.send(
                "/app/chat-rooms/" + CHAT_ROOM_ID + "/messages",
                new ChatTextMessageRequest(CONTENT)
        );

        verify(chatMessageService, timeout(5_000)).sendTextMessage(
                BUYER_ID,
                CHAT_ROOM_ID,
                CONTENT
        );

        JsonNode buyerMessage =
                buyerMessages.poll(5, TimeUnit.SECONDS);
        JsonNode sellerMessage =
                sellerMessages.poll(5, TimeUnit.SECONDS);

        assertMessage(buyerMessage, true, createdAt);
        assertMessage(sellerMessage, false, createdAt);
    }

    private StompSession connect(Long userId) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(
                "Authorization",
                "Bearer " + jwtProvider.createAccessToken(userId)
        );

        WebSocketHttpHeaders handshakeHeaders =
                new WebSocketHttpHeaders();
        handshakeHeaders.setOrigin("http://localhost:3000");

        StompSession session = stompClient.connectAsync(
                "ws://localhost:" + port + "/ws-chat",
                handshakeHeaders,
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(5, TimeUnit.SECONDS);

        sessions.add(session);
        return session;
    }

    private BlockingQueue<JsonNode> subscribe(
            StompSession session,
            Long userId
    ) throws InterruptedException {
        BlockingQueue<JsonNode> messages =
                new LinkedBlockingQueue<>();

        session.subscribe(
                SUBSCRIPTION_DESTINATION,
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return JsonNode.class;
                    }

                    @Override
                    public void handleFrame(
                            StompHeaders headers,
                            Object payload
                    ) {
                        messages.add((JsonNode) payload);
                    }
                }
        );

        assertThat(awaitSubscription(userId)).isTrue();
        return messages;
    }

    private boolean awaitSubscription(Long userId)
            throws InterruptedException {
        long deadline = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(5);

        while (System.nanoTime() < deadline) {
            boolean subscribed = userRegistry
                    .getUsers()
                    .stream()
                    .filter(user -> user.getName().equals(userId.toString()))
                    .flatMap(user -> user.getSessions().stream())
                    .flatMap(session -> session.getSubscriptions().stream())
                    .anyMatch(subscription ->
                            SUBSCRIPTION_DESTINATION.equals(
                                    subscription.getDestination()
                            )
                    );

            if (subscribed) {
                return true;
            }
            Thread.sleep(20);
        }

        return false;
    }

    private void assertMessage(
            JsonNode message,
            boolean mine,
            LocalDateTime createdAt
    ) {
        assertThat(message).isNotNull();
        assertThat(message.get("chatRoomId").asLong())
                .isEqualTo(CHAT_ROOM_ID);
        assertThat(message.get("messageId").asLong())
                .isEqualTo(MESSAGE_ID);
        assertThat(message.get("senderId").asLong())
                .isEqualTo(BUYER_ID);
        assertThat(message.get("messageType").asText())
                .isEqualTo(MessageType.TEXT.name());
        assertThat(message.get("content").asText())
                .isEqualTo(CONTENT);
        assertThat(message.get("mine").asBoolean())
                .isEqualTo(mine);
        assertThat(message.get("read").asBoolean()).isFalse();
        assertThat(
                LocalDateTime.parse(
                        message.get("createdAt").asText()
                )
        ).isEqualTo(createdAt);
    }
}
