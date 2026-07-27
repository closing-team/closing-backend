package com.closing.closing.domain.chat.controller;

import com.closing.closing.domain.chat.dto.request.ChatRoomListRequest;
import com.closing.closing.domain.chat.dto.request.MessageHistoryRequest;
import com.closing.closing.domain.chat.dto.response.ChatRoomListResponse;
import com.closing.closing.domain.chat.dto.response.MessageHistoryListResponse;
import com.closing.closing.domain.chat.service.ChatMessageService;
import com.closing.closing.domain.chat.service.ChatRoomService;
import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import com.closing.closing.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    private static final Long AUTHENTICATED_USER_ID = 42L;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageService chatMessageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        AUTHENTICATED_USER_ID,
                        null,
                        List.of()
                )
        );

        ChatRoomController controller =
                new ChatRoomController(chatRoomService, chatMessageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PATCH /api/v1/chat-rooms/{id}/read 요청으로 메시지를 읽음 처리한다")
    void readMessages_success() throws Exception {
        // 채팅방 ID가 읽음 처리 Service에 전달되는지 검증한다.
        mockMvc.perform(patch("/api/v1/chat-rooms/10/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(chatMessageService).readMessage(AUTHENTICATED_USER_ID, 10L);
    }

    @Test
    @DisplayName("GET /api/v1/chat-rooms/{id}/messages 요청으로 메시지 히스토리를 조회한다")
    void getMessages_success() throws Exception {
        // 채팅방 ID와 메시지 커서가 히스토리 조회 Service에 전달되는지 검증한다.
        MessageHistoryListResponse<Long> response =
                new MessageHistoryListResponse<>(
                        List.of(),
                        CursorPageResponse.of(20L, true)
                );
        given(chatMessageService.getMessageHistoryList(
                any(MessageHistoryRequest.class), eq(10L), eq(AUTHENTICATED_USER_ID)
        )).willReturn(response);

        mockMvc.perform(get("/api/v1/chat-rooms/10/messages")
                        .param("cursor", "40")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.page.nextCursor").value(20))
                .andExpect(jsonPath("$.data.page.hasNext").value(true));

        verify(chatMessageService).getMessageHistoryList(
                argThat(request -> request.getCursor().equals(40L)
                        && request.getSize() == 20),
                eq(10L),
                eq(AUTHENTICATED_USER_ID)
        );
    }

    @Test
    @DisplayName("GET /api/v1/chat-rooms 요청으로 채팅방 목록을 조회한다")
    void getChatRooms_success() throws Exception {
        // 복합 커서와 조회 크기가 채팅방 목록 Service에 전달되는지 검증한다.
        String cursor = "2026-07-22T11:00|200";
        ChatRoomListResponse<String> response =
                new ChatRoomListResponse<>(
                        List.of(),
                        CursorPageResponse.of(null, false)
                );
        given(chatRoomService.getChatRooms(
                eq(AUTHENTICATED_USER_ID),
                any(ChatRoomListRequest.class)
        ))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/chat-rooms")
                        .param("cursor", cursor)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatRooms").isArray())
                .andExpect(jsonPath("$.data.page.hasNext").value(false));

        verify(chatRoomService).getChatRooms(
                eq(AUTHENTICATED_USER_ID),
                argThat(request -> cursor.equals(request.getCursor())
                        && request.getSize() == 20)
        );
    }

    @Test
    @DisplayName("채팅방 목록의 size가 허용 범위를 벗어나면 400을 반환한다")
    void getChatRooms_invalidSize() throws Exception {
        // Controller에서 @Valid가 query string의 크기 제한을 적용하는지 검증한다.
        mockMvc.perform(get("/api/v1/chat-rooms")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
