package com.closing.closing.domain.chat.repository;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.ChatRoom;

public interface ChatRoomListProjection {

    ChatRoom getChatRoom();

    ChatMessage getLastMessage();

    Long getUnreadMessageCount();
}
