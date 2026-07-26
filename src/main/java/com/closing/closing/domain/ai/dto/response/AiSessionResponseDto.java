package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 세션 시작 응답")
public record AiSessionResponseDto(
        @Schema(
                description = "생성되거나 기존에 확정된 세션 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String sessionId,

        @Schema(
                description = "세션 상태",
                example = "NEW",
                allowableValues = {"NEW", "GENERATED", "ALREADY_CONFIRMED"})
        String status,

        @Schema(
                description = "AI 응답 메시지. 세션이 이미 확정된 경우 null입니다.",
                example = "언제 폐업할 예정인가요?",
                types = {"string", "null"})
        String aiMessage,

        @Schema(description = "현재까지 진행된 대화 턴 수", example = "1")
        int turnCount,

        @Schema(
                description = "AI가 생성한 임시 일정 목록. 아직 생성되지 않았다면 null입니다.",
                types = {"array", "null"})
        List<AiGeneratedTaskDto> generatedTasks
) {}