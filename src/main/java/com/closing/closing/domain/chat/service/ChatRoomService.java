package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.request.ChatRoomListRequest;
import com.closing.closing.domain.chat.dto.response.ChatRoomCreateResponse;
import com.closing.closing.domain.chat.dto.response.ChatRoomListResponse;
import com.closing.closing.domain.chat.dto.response.ChatRoomResponse;
import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.repository.ChatRoomListProjection;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.product.dto.response.CursorPageResponse;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    @Transactional
    public ChatRoomCreateResponse createChatRoom(
            Long userId,
            Long productId
    ) {
        // 존재하는 상품인지 조회
        Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 존재하는 사용자인지 조회
        User buyer = entityManager.find(User.class, userId);

        if (buyer == null || buyer.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 자기 상품인지 확인
        boolean isOwner = product.getSeller().getId().equals(userId);
        if (isOwner) {
            throw new CustomException(ErrorCode.SELF_CHAT_NOT_ALLOWED);
        }

        // 기존에 존재하는 채팅방인지 조회, 존재한다면 반환
        Optional<ChatRoom> existingChatRoom =
                chatRoomRepository
                        .findByProductIdAndBuyerId(
                                productId,
                                userId
                        );
        if (existingChatRoom.isPresent()) {
            return ChatRoomCreateResponse.from(
                    existingChatRoom.get()
            );
        }

        // 기존 채팅방이 없는데 판매 완료 상품이라면 신규 생성 불가
        if (product.getStatus() == ProductStatus.SOLD_OUT) {
            throw new CustomException(ErrorCode.CHAT_PRODUCT_NOT_AVAILABLE);
        }

        // 판매중이거나 예약중이고, 기존 채팅방이 없으면 신규 채팅방 생성
        return createNewChatRoom(buyer, product);
    }

    // 새로운 채팅방을 만드는 메서드
    private ChatRoomCreateResponse createNewChatRoom(
            User buyer,
            Product product
    ) {

        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .buyer(buyer)
                .seller(product.getSeller())
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomCreateResponse.from(savedChatRoom);
    }

    // 채팅방 목록 조회
    public ChatRoomListResponse<String> getChatRooms(
            Long userId,
            ChatRoomListRequest request
    ) {
        // 사용자 조회, 검증
        User user = entityManager.find(User.class, userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // String cursor 파싱
        ChatRoomCursor cursor = parseChatRoomCursor(request.getCursor());

        Pageable pageable = PageRequest.of(
                0,
                request.getSize() + 1
        );

        List<ChatRoomListProjection> results =
                chatRoomRepository.findChatRooms(
                        userId,
                        cursor.lastMessageAt(),
                        cursor.lastMessageId(),
                        pageable
                );

        boolean hasNext = results.size() > request.getSize();

        List<ChatRoomListProjection> pageResults = hasNext
                ? results.subList(0, request.getSize())
                : results;

        List<ChatRoomResponse> chatRoomResponses =
                pageResults.stream()
                        .map(result -> ChatRoomResponse.from(
                                result.getChatRoom(),
                                user,
                                result.getLastMessage(),
                                Math.toIntExact(result.getUnreadMessageCount())
                        ))
                        .toList();

        String nextCursor = null;

        if (hasNext && !pageResults.isEmpty()) {
            ChatMessage lastMessage = pageResults
                    .get(pageResults.size() - 1)
                    .getLastMessage();

            nextCursor =
                    lastMessage.getCreatedAt()
                            + "|"
                            + lastMessage.getId();
        }

        CursorPageResponse<String> pageResponse =
                CursorPageResponse.of(nextCursor, hasNext);

        return new ChatRoomListResponse<>(
                chatRoomResponses,
                pageResponse
        );
    }

    // 채팅방 조회 String cursor 파싱 메서드
    private ChatRoomCursor parseChatRoomCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new ChatRoomCursor(null, null);
        }

        try {
            String[] values = cursor.split("\\|", 2);

            if (values.length != 2) {
                throw new CustomException(ErrorCode.INVALID_CURSOR);
            }

            return new ChatRoomCursor(
                    LocalDateTime.parse(values[0]),
                    Long.parseLong(values[1])
            );
        } catch (DateTimeParseException | NumberFormatException exception) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    private record ChatRoomCursor(
            LocalDateTime lastMessageAt,
            Long lastMessageId
    ) {
    }


}
