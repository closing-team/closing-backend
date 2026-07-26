package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "확정된 AI 세션 조회 응답")
public record AiSessionConfirmedResponseDto(
        @Schema(
                description = "세션 ID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String sessionId,

        @Schema(
                description = "세션 상태",
                example = "ALREADY_CONFIRMED",
                allowableValues = {"ALREADY_CONFIRMED"})
        String status,

        @Schema(description = "확정된 일정 목록")
        List<AiConfirmedTaskDto> confirmedTasks
) implements AiSessionDetailResponseDto {}