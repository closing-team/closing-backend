package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.request.MessageHistoryRequest;
import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.chat.repository.ChatMessageRepository;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long CHAT_ROOM_ID = 10L;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private User buyer;
    private User seller;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        buyer = createUser(BUYER_ID, "구매자");
        seller = createUser(SELLER_ID, "판매자");
        chatRoom = ChatRoom.builder()
                .buyer(buyer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);
    }

    @Test
    @DisplayName("채팅방 참여자가 입장하면 상대방의 모든 미읽음 메시지를 읽음 처리한다")
    void readMessage_success() {
        // 채팅방 확인 후 참여자 자신이 아닌 발신자의 메시지만 갱신하는지 검증한다.
        given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));

        chatMessageService.readMessage(BUYER_ID, CHAT_ROOM_ID);

        verify(chatMessageRepository)
                .markAllUnreadMessagesAsRead(CHAT_ROOM_ID, BUYER_ID);
    }

    @Test
    @DisplayName("채팅방 참여자가 아니면 메시지를 읽음 처리할 수 없다")
    void readMessage_forbidden() {
        // 구매자와 판매자가 아닌 사용자의 읽음 처리 요청을 차단하는지 검증한다.
        given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));

        assertErrorCode(
                () -> chatMessageService.readMessage(999L, CHAT_ROOM_ID),
                ErrorCode.CHAT_ROOM_ACCESS_FORBIDDEN
        );

        verify(chatMessageRepository, never())
                .markAllUnreadMessagesAsRead(any(), any());
    }

    @Test
    @DisplayName("메시지 히스토리는 요청 크기만 반환하고 오래된 순서로 정렬한다")
    void getMessageHistoryList_cursorPaging() {
        // size보다 한 건 더 조회하여 다음 커서와 화면 표시 순서를 계산하는지 검증한다.
        MessageHistoryRequest request = new MessageHistoryRequest();
        request.setCursor(40L);
        request.setSize(2);

        ChatMessage newest = createMessage(30L, buyer, "최신 메시지", 12);
        ChatMessage older = createMessage(20L, seller, "이전 메시지", 11);
        ChatMessage extra = createMessage(10L, seller, "다음 페이지 메시지", 10);

        given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findMessageHistory(
                eq(CHAT_ROOM_ID), eq(40L), any(Pageable.class)
        )).willReturn(List.of(newest, older, extra));

        var response = chatMessageService.getMessageHistoryList(
                request,
                CHAT_ROOM_ID,
                BUYER_ID
        );

        assertThat(response.getMessages())
                .extracting("messageId")
                .containsExactly(20L, 30L);
        assertThat(response.getMessages().get(0).isMine()).isFalse();
        assertThat(response.getMessages().get(1).isMine()).isTrue();
        assertThat(response.getPage().isHasNext()).isTrue();
        assertThat(response.getPage().getNextCursor()).isEqualTo(20L);

        verify(chatMessageRepository).findMessageHistory(
                eq(CHAT_ROOM_ID),
                eq(40L),
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 3)
        );
    }

    @Test
    @DisplayName("채팅방이 없으면 메시지 히스토리 조회에 실패한다")
    void getMessageHistoryList_roomNotFound() {
        // 존재하지 않는 채팅방에서는 메시지 조회 쿼리를 실행하지 않는지 검증한다.
        MessageHistoryRequest request = new MessageHistoryRequest();
        given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.empty());

        assertErrorCode(
                () -> chatMessageService.getMessageHistoryList(
                        request,
                        CHAT_ROOM_ID,
                        BUYER_ID
                ),
                ErrorCode.CHAT_ROOM_NOT_FOUND
        );

        verify(chatMessageRepository, never())
                .findMessageHistory(any(), any(), any());
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .kakaoId("kakao-" + id)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ChatMessage createMessage(
            Long id,
            User sender,
            String content,
            int hour
    ) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(
                message,
                "createdAt",
                LocalDateTime.of(2026, 7, 22, hour, 0)
        );
        return message;
    }

    private void assertErrorCode(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
