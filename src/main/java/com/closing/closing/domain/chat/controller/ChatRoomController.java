package com.closing.closing.domain.chat.controller;

import com.closing.closing.domain.chat.dto.request.ChatRoomListRequest;
import com.closing.closing.domain.chat.dto.request.MessageHistoryRequest;
import com.closing.closing.domain.chat.dto.request.MessageRequest;
import com.closing.closing.domain.chat.dto.response.ChatRoomCreateResponse;
import com.closing.closing.domain.chat.dto.response.ChatRoomListResponse;
import com.closing.closing.domain.chat.dto.response.MessageHistoryListResponse;
import com.closing.closing.domain.chat.dto.response.MessageSendResponse;
import com.closing.closing.domain.chat.service.ChatMessageService;
import com.closing.closing.domain.chat.service.ChatRoomService;
import com.closing.closing.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/{productId}")
    public ApiResponse<ChatRoomCreateResponse> createChatRoom(
            @PathVariable("productId") Long productId
    ) {

        // TODO: 인증 연결 이후 인증 객체에서 추출
        Long userId = 1L;

        ChatRoomCreateResponse response = chatRoomService.createChatRoom(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/{chatRoomId}/messages")
    public ApiResponse<MessageSendResponse> sendMessages(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestPart(value = "content", required = false) MessageRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {

        // TODO: 인증
        Long userId = 1L;

        MessageSendResponse response = chatMessageService.sendMessage(
                userId,
                chatRoomId,
                request,
                images
        );

        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{chatRoomId}/read")
    public ApiResponse<Void> readMessages(
            @PathVariable("chatRoomId") Long chatRoomId
    ) {
        // TODO: 인증
        Long userId = 1L;

        chatMessageService.readMessage(userId, chatRoomId);

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{chatRoomId}/messages")
    public ApiResponse<MessageHistoryListResponse<Long>> getMessages(
            @PathVariable("chatRoomId") Long chatRoomId,
            @Valid @ModelAttribute MessageHistoryRequest request
    ) {
        Long userId = 1L;

        MessageHistoryListResponse<Long> response =
                chatMessageService.getMessageHistoryList(request, chatRoomId, userId);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping
    public ApiResponse<ChatRoomListResponse<String>> getChatRooms(
            @Valid @ModelAttribute ChatRoomListRequest request
    ) {
        Long userId = 1L;

        ChatRoomListResponse<String> response =
                chatRoomService.getChatRooms(userId, request);

        return ApiResponse.onSuccess(response);
    }

}
