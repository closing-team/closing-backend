package com.closing.closing.domain.chat.repository;

import com.closing.closing.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatMessage message
            SET message.isRead = true
            WHERE message.chatRoom.id = :chatRoomId
              AND message.sender.id <> :readerId
              AND message.isRead = false
            """)
    int markAllUnreadMessagesAsRead(
            @Param("chatRoomId") Long chatRoomId,
            @Param("readerId") Long readerId
    );
}
