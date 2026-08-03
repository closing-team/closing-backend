package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;
import com.closing.closing.domain.chat.entity.MessageType;
import com.closing.closing.domain.product.entity.BusinessCategory;
import com.closing.closing.domain.product.entity.Product;
import com.closing.closing.domain.product.entity.ProductCategory;
import com.closing.closing.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomResponseMappingTest {

    private static final BigDecimal LATITUDE = new BigDecimal("37.5665");
    private static final BigDecimal LONGITUDE = new BigDecimal("126.9780");

    @Test
    @DisplayName("채팅방 목록 응답은 직거래 장소를 거리 없이 반환한다")
    void chatRoomListResponse_directProduct_returnsTradeLocationWithoutDistance() {
        User buyer = createUser(1L, "구매자");
        User seller = createUser(2L, "판매자");
        Product product = createProduct(100L, seller, true, List.of("thumbnail.jpg"));
        ChatRoom chatRoom = createChatRoom(10L, product, buyer, seller);
        ChatMessage lastMessage = createLastMessage(chatRoom, seller);

        ChatRoomResponse response = ChatRoomResponse.from(
                chatRoom,
                buyer,
                lastMessage,
                3
        );

        assertDirectTradeLocation(response.getProduct());
    }

    @Test
    @DisplayName("채팅방 생성 응답은 목록 응답과 동일한 직거래 장소 구조를 사용한다")
    void chatRoomCreateResponse_directProduct_returnsSharedTradeLocation() {
        User buyer = createUser(1L, "구매자");
        User seller = createUser(2L, "판매자");
        Product product = createProduct(100L, seller, true, List.of("thumbnail.jpg"));
        ChatRoom chatRoom = createChatRoom(10L, product, buyer, seller);

        ChatRoomCreateResponse response = ChatRoomCreateResponse.from(chatRoom);

        assertDirectTradeLocation(response.getProduct());
    }

    @Test
    @DisplayName("직거래 미지원 상품은 목록과 생성 응답에서 직거래 정보를 null로 반환한다")
    void nonDirectProduct_returnsNullTradeLocation() {
        User buyer = createUser(1L, "구매자");
        User seller = createUser(2L, "판매자");
        Product product = createProduct(100L, seller, false, List.of("thumbnail.jpg"));
        ChatRoom chatRoom = createChatRoom(10L, product, buyer, seller);
        ChatMessage lastMessage = createLastMessage(chatRoom, seller);

        ChatRoomResponse listResponse = ChatRoomResponse.from(
                chatRoom,
                buyer,
                lastMessage,
                0
        );
        ChatRoomCreateResponse createResponse = ChatRoomCreateResponse.from(chatRoom);

        assertThat(listResponse.getProduct().getTradeLocation()).isNull();
        assertThat(createResponse.getProduct().getTradeLocation()).isNull();
    }

    @Test
    @DisplayName("상품 이미지가 없으면 대표 이미지 URL을 null로 반환한다")
    void productWithoutImages_returnsNullThumbnail() {
        User seller = createUser(2L, "판매자");
        Product emptyImagesProduct = createProduct(100L, seller, false, List.of());
        Product nullImagesProduct = createProduct(101L, seller, false, null);

        assertThat(ChatRoomProductResponse.from(emptyImagesProduct).getThumbnailUrl()).isNull();
        assertThat(ChatRoomProductResponse.from(nullImagesProduct).getThumbnailUrl()).isNull();
    }

    private void assertDirectTradeLocation(ChatRoomProductResponse productResponse) {
        assertThat(productResponse.getTradeLocation()).isNotNull();
        assertThat(productResponse.getTradeLocation().getDistrict()).isEqualTo("서울특별시 중구 명동");
        assertThat(productResponse.getTradeLocation().getLatitude()).isEqualByComparingTo(LATITUDE);
        assertThat(productResponse.getTradeLocation().getLongitude()).isEqualByComparingTo(LONGITUDE);
        assertThat(productResponse.getTradeLocation().getDistanceKm()).isNull();
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .kakaoId("kakao-" + id)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Product createProduct(
            Long id,
            User seller,
            boolean directAvailable,
            List<String> imageUrls
    ) {
        Product product = Product.builder()
                .seller(seller)
                .title("중고 의자")
                .businessCategory(BusinessCategory.KOREAN_MEAL)
                .productCategory(ProductCategory.CHAIR_SOFA_BAR_CHAIR)
                .price(100_000)
                .imageUrls(imageUrls)
                .isDeliveryAvailable(true)
                .isDirectAvailable(directAvailable)
                .tradeLocation(directAvailable ? "서울특별시 중구 명동" : null)
                .latitude(directAvailable ? LATITUDE : null)
                .longitude(directAvailable ? LONGITUDE : null)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private ChatRoom createChatRoom(
            Long id,
            Product product,
            User buyer,
            User seller
    ) {
        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", id);
        return chatRoom;
    }

    private ChatMessage createLastMessage(ChatRoom chatRoom, User sender) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content("아직 판매 중인가요?")
                .messageType(MessageType.TEXT)
                .build();
        ReflectionTestUtils.setField(
                message,
                "createdAt",
                LocalDateTime.of(2026, 8, 3, 15, 10)
        );
        return message;
    }
}
