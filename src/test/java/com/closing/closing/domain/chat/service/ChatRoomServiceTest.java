package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.request.ChatRoomListRequest;
import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.chat.repository.ChatRoomListProjection;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private User user;
    private User otherMember;
    private Product product;

    @BeforeEach
    void setUp() {
        user = createUser(USER_ID, "나");
        otherMember = createUser(2L, "상대방");
        product = createProduct(100L, otherMember);
    }

    @Test
    @DisplayName("채팅방 목록은 요청 크기만 반환하고 마지막 메시지로 다음 커서를 만든다")
    void getChatRooms_cursorPaging() {
        // size보다 한 건 더 조회해 hasNext와 복합 커서를 계산하는지 검증한다.
        ChatRoomListRequest request = new ChatRoomListRequest();
        request.setSize(2);

        ChatRoomListProjection first = createProjection(30L, 300L, 12, "최신", 2L);
        ChatRoomListProjection second = createProjection(20L, 200L, 11, "이전", 1L);
        ChatRoomListProjection extra = createProjection(10L, 100L, 10, "다음", 0L);

        given(entityManager.find(User.class, USER_ID)).willReturn(user);
        given(chatRoomRepository.findChatRooms(
                eq(USER_ID), isNull(), isNull(), any(Pageable.class)
        )).willReturn(List.of(first, second, extra));

        var response = chatRoomService.getChatRooms(USER_ID, request);

        assertThat(response.getChatRooms()).hasSize(2);
        assertThat(response.getChatRooms().get(0).getLastMessage()).isEqualTo("최신");
        assertThat(response.getChatRooms().get(0).getUnReadMessagesCount()).isEqualTo(2);
        assertThat(response.getChatRooms().get(0).getOtherMember().getMemberId())
                .isEqualTo(otherMember.getId());
        assertThat(response.getPage().isHasNext()).isTrue();
        assertThat(response.getPage().getNextCursor())
                .isEqualTo("2026-07-22T11:00|200");

        verify(chatRoomRepository).findChatRooms(
                eq(USER_ID),
                isNull(),
                isNull(),
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 3)
        );
    }

    @Test
    @DisplayName("채팅방 목록의 복합 커서를 시간과 메시지 ID로 변환한다")
    void getChatRooms_parseCursor() {
        // 프론트가 반환한 커서를 다음 Repository 조회 조건으로 전달하는지 검증한다.
        ChatRoomListRequest request = new ChatRoomListRequest();
        request.setCursor("2026-07-22T11:00|200");

        given(entityManager.find(User.class, USER_ID)).willReturn(user);
        given(chatRoomRepository.findChatRooms(
                eq(USER_ID),
                eq(LocalDateTime.of(2026, 7, 22, 11, 0)),
                eq(200L),
                any(Pageable.class)
        )).willReturn(List.of());

        var response = chatRoomService.getChatRooms(USER_ID, request);

        assertThat(response.getChatRooms()).isEmpty();
        assertThat(response.getPage().isHasNext()).isFalse();
        assertThat(response.getPage().getNextCursor()).isNull();
    }

    @Test
    @DisplayName("채팅방 목록 커서 형식이 잘못되면 조회에 실패한다")
    void getChatRooms_invalidCursor() {
        // 잘못된 시간 또는 메시지 ID를 Repository에 전달하기 전에 차단하는지 검증한다.
        ChatRoomListRequest request = new ChatRoomListRequest();
        request.setCursor("invalid-cursor");
        given(entityManager.find(User.class, USER_ID)).willReturn(user);

        assertErrorCode(
                () -> chatRoomService.getChatRooms(USER_ID, request),
                ErrorCode.INVALID_CURSOR
        );

        verify(chatRoomRepository, never())
                .findChatRooms(any(), any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 채팅방 목록을 조회할 수 없다")
    void getChatRooms_userNotFound() {
        // 사용자 검증 실패 시 채팅방 조회를 실행하지 않는지 검증한다.
        given(entityManager.find(User.class, USER_ID)).willReturn(null);

        assertErrorCode(
                () -> chatRoomService.getChatRooms(USER_ID, new ChatRoomListRequest()),
                ErrorCode.USER_NOT_FOUND
        );

        verify(chatRoomRepository, never())
                .findChatRooms(any(), any(), any(), any());
    }

    private ChatRoomListProjection createProjection(
            Long chatRoomId,
            Long messageId,
            int hour,
            String content,
            Long unreadMessageCount
    ) {
        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .buyer(user)
                .seller(otherMember)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        ChatMessage lastMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(otherMember)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();
        ReflectionTestUtils.setField(lastMessage, "id", messageId);
        ReflectionTestUtils.setField(
                lastMessage,
                "createdAt",
                LocalDateTime.of(2026, 7, 22, hour, 0)
        );

        return new ChatRoomListProjection() {
            @Override
            public ChatRoom getChatRoom() {
                return chatRoom;
            }

            @Override
            public ChatMessage getLastMessage() {
                return lastMessage;
            }

            @Override
            public Long getUnreadMessageCount() {
                return unreadMessageCount;
            }
        };
    }

    private User createUser(Long id, String nickname) {
        User createdUser = User.builder()
                .kakaoId("kakao-" + id)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(createdUser, "id", id);
        return createdUser;
    }

    private Product createProduct(Long id, User seller) {
        Product createdProduct = Product.builder()
                .seller(seller)
                .title("중고 의자")
                .businessCategory(BusinessCategory.KOREAN_MEAL)
                .productCategory(ProductCategory.CHAIR_SOFA_BAR_CHAIR)
                .price(100_000)
                .imageUrls(List.of("thumbnail.jpg"))
                .build();
        ReflectionTestUtils.setField(createdProduct, "id", id);
        return createdProduct;
    }

    private void assertErrorCode(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
