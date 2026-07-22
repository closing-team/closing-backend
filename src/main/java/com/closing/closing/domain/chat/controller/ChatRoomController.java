package com.closing.closing.domain.chat.controller;

import com.closing.closing.domain.chat.dto.response.ChatRoomCreateResponse;
import com.closing.closing.domain.chat.service.ChatRoomService;
import com.closing.closing.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/{productId}")
    public ApiResponse<ChatRoomCreateResponse> createChatRoom(
            @PathVariable("productId") Long productId
    ) {

        // TODO: 인증 연결 이후 인증 객체에서 추출
        Long userId = 1L;

        ChatRoomCreateResponse response = chatRoomService.createChatRoom(userId, productId);

        return ApiResponse.onSuccess(response);
    }

}
