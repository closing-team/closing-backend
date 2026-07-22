package com.closing.closing.domain.chat.service;

import com.closing.closing.domain.chat.dto.response.ChatRoomCreateResponse;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.repository.ChatRoomRepository;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductStatus;
import com.closing.closing.domain.product.repository.ProductRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
