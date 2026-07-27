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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ChatRoom", description = "중고거래 채팅방 및 메시지 API")
@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(
            summary = "채팅방 생성",
            description = """
                    상품 판매자와의 채팅방을 생성합니다.
                    동일한 상품과 구매자로 생성된 채팅방이 이미 있으면 기존 채팅방을 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅방 생성 또는 기존 채팅방 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @PostMapping("/{productId}")
    public ApiResponse<ChatRoomCreateResponse> createChatRoom(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "문의할 상품 ID",
                    example = "15",
                    required = true
            )
            @PathVariable("productId") Long productId
    ) {

        ChatRoomCreateResponse response = chatRoomService.createChatRoom(userId, productId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "채팅 메시지 전송",
            description = """
                    채팅방에 텍스트 또는 이미지를 전송합니다.
                    텍스트와 이미지는 동시에 보낼 수 없으며 둘 중 하나는 반드시 전달해야 합니다.
                    이미지 여러 장을 보내면 이미지마다 별도의 메시지가 생성됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅 메시지 전송 성공",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "채팅방 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "code": "CHAT_ROOM_NOT_FOUND",
                                              "message": "채팅방을 찾을 수 없습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping(
            value = "/{chatRoomId}/messages",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<MessageSendResponse> sendMessages(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "메시지를 전송할 채팅방 ID",
                    example = "12",
                    required = true
            )
            @PathVariable("chatRoomId") Long chatRoomId,
            @Parameter(
                    description = """
                            텍스트 메시지 정보 JSON입니다. Content-Type은 application/json입니다.<br><br>
                            **content**: 전송할 텍스트 / 예시: 아직 판매 중인가요?<br><br>
                            텍스트 메시지를 보낼 때만 전달하며 이미지를 보낼 때는 생략합니다.
                            """,
                    required = false,
                    schema = @Schema(implementation = MessageRequest.class)
            )
            @RequestPart(value = "content", required = false) MessageRequest request,
            @Parameter(
                    description = """
                            전송할 이미지 파일 목록입니다.<br><br>
                            예시: chat-image1.jpg, chat-image2.jpg<br><br>
                            텍스트를 전송할 때는 생략합니다. 각 이미지가 별도의 메시지로 생성됩니다.
                            """,
                    required = false,
                    array = @ArraySchema(
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {

        MessageSendResponse response = chatMessageService.sendMessage(
                userId,
                chatRoomId,
                request,
                images
        );

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "채팅 메시지 읽음 처리",
            description = "채팅방에서 상대방이 보낸 모든 미읽음 메시지를 읽음 상태로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅 메시지 읽음 처리 성공",
                    useReturnTypeSchema = true
            )
    })
    @PatchMapping("/{chatRoomId}/read")
    public ApiResponse<Void> readMessages(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "메시지를 읽음 처리할 채팅방 ID",
                    example = "12",
                    required = true
            )
            @PathVariable("chatRoomId") Long chatRoomId
    ) {

        chatMessageService.readMessage(userId, chatRoomId);

        return ApiResponse.onSuccess(null);
    }

    @Operation(
            summary = "채팅 메시지 히스토리 조회",
            description = "채팅방의 메시지를 메시지 ID 커서 기반으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅 메시지 히스토리 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping("/{chatRoomId}/messages")
    public ApiResponse<MessageHistoryListResponse<Long>> getMessages(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "메시지 히스토리를 조회할 채팅방 ID",
                    example = "12",
                    required = true
            )
            @PathVariable("chatRoomId") Long chatRoomId,
            @ParameterObject
            @Valid @ModelAttribute MessageHistoryRequest request
    ) {

        MessageHistoryListResponse<Long> response =
                chatMessageService.getMessageHistoryList(request, chatRoomId, userId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "채팅방 목록 조회",
            description = "현재 사용자가 참여 중인 채팅방을 최근 메시지 순으로 커서 기반 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping
    public ApiResponse<ChatRoomListResponse<String>> getChatRooms(
            @AuthenticationPrincipal Long userId,
            @ParameterObject
            @Valid @ModelAttribute ChatRoomListRequest request
    ) {

        ChatRoomListResponse<String> response =
                chatRoomService.getChatRooms(userId, request);

        return ApiResponse.onSuccess(response);
    }

}
