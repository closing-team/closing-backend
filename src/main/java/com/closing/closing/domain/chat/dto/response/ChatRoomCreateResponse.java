package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ChatRoomCreateResponse {

    private final Long chatRoomId;

    private final ChatRoomProductResponse product;

    private final ChatRoomOtherMemberResponse otherMember;

    private final LocalDateTime createdAt;

    public static ChatRoomCreateResponse from(ChatRoom chatRoom) {

        Product product = chatRoom.getProduct();
        User seller = chatRoom.getSeller();

        List<String> imageUrls = product.getImageUrls();

        String thumbnailUrl = imageUrls == null || imageUrls.isEmpty()
                ? null
                : imageUrls.get(0);

        ChatRoomProductResponse productResponse =
                new ChatRoomProductResponse(
                        product.getId(),
                        product.getTitle(),
                        thumbnailUrl,
                        product.getPrice(),
                        product.getStatus()
                );

        ChatRoomOtherMemberResponse otherMemberResponse =
                new ChatRoomOtherMemberResponse(
                        seller.getId(),
                        seller.getNickname(),
                        seller.getProfileImageUrl()
                );

        return new ChatRoomCreateResponse(
                chatRoom.getId(),
                productResponse,
                otherMemberResponse,
                chatRoom.getCreatedAt()
        );

    }


}
