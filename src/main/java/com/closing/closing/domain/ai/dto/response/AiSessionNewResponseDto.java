package com.closing.closing.domain.ai.dto.response;

import com.closing.closing.domain.ai.dto.AiMessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "아직 일정이 생성되지 않은 AI 세션 조회 응답")
public record AiSessionNewResponseDto(
        @Schema(
                description = "세션 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String sessionId,

        @Schema(
                description = "세션 상태",
                example = "NEW",
                allowableValues = {"NEW"})
        String status,

        @Schema(description = "현재까지 진행된 대화 턴 수", example = "1")
        int turnCount,

        @Schema(description = "남은 질문 횟수", example = "9")
        int remainingTurns,

        @Schema(description = "지금까지 주고받은 대화 메시지 목록")
        List<AiMessageDto> messages
) implements AiSessionDetailResponseDto {}