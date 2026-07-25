package com.closing.closing.domain.chat.dto.response;

import com.closing.closing.domain.chat.entity.ChatMessage;
import com.closing.closing.domain.chat.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 정보")
@Getter
@RequiredArgsConstructor
public class MessageResponse {

    @Schema(description = "메시지 ID", example = "105")
    private final Long messageId;

    @Schema(description = "메시지 발신자 회원 ID", example = "7")
    private final Long senderId;

    @Schema(description = "메시지 유형", example = "TEXT")
    private final MessageType messageType;

    @Schema(
            description = "메시지 내용. TEXT이면 텍스트이고 IMAGE이면 S3 이미지 URL입니다.",
            example = "안녕하세요. 아직 판매 중인가요?"
    )
    private final String content;

    @Schema(description = "현재 사용자가 보낸 메시지인지 여부", example = "true")
    private final boolean mine;

    @Schema(description = "상대방의 메시지 읽음 여부", example = "false")
    private final boolean isRead;

    @Schema(description = "메시지 전송 일시", example = "2026-07-24T15:10:00")
    private final LocalDateTime createdAt;

    public static MessageResponse from(
            ChatMessage chatMessage,
            Long userId
    ) {
        return new MessageResponse(
                chatMessage.getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.getSender().getId().equals(userId),
                chatMessage.isRead(),
                chatMessage.getCreatedAt()
        );
    }

}
