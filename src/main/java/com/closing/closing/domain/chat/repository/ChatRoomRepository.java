package com.closing.closing.domain.chat.repository;

import com.closing.closing.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductIdAndBuyerId(
            Long productId,
            Long buyerId
    );

    @Query("""
            SELECT chatRoom AS chatRoom,
                   lastMessage AS lastMessage,
                   COUNT(unreadMessage.id) AS unreadMessageCount
            FROM ChatRoom chatRoom
            JOIN ChatMessage lastMessage
              ON lastMessage.chatRoom = chatRoom
            LEFT JOIN ChatMessage unreadMessage
              ON unreadMessage.chatRoom = chatRoom
             AND unreadMessage.sender.id <> :userId
             AND unreadMessage.isRead = false
            WHERE (chatRoom.buyer.id = :userId OR chatRoom.seller.id = :userId)
              AND lastMessage.id = (
                  SELECT MAX(message.id)
                  FROM ChatMessage message
                  WHERE message.chatRoom = chatRoom
              )
              AND (
                  :cursorLastMessageAt IS NULL
                  OR lastMessage.createdAt < :cursorLastMessageAt
                  OR (
                      lastMessage.createdAt = :cursorLastMessageAt
                      AND lastMessage.id < :cursorLastMessageId
                  )
              )
            GROUP BY chatRoom, lastMessage
            ORDER BY lastMessage.createdAt DESC, lastMessage.id DESC
            """)
    List<ChatRoomListProjection> findChatRooms(
            @Param("userId") Long userId,
            @Param("cursorLastMessageAt") LocalDateTime cursorLastMessageAt,
            @Param("cursorLastMessageId") Long cursorLastMessageId,
            Pageable pageable
    );
}
