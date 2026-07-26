package com.closing.closing.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "AI가 생성한 임시 일정")
public record AiGeneratedTaskDto(
        @Schema(description = "임시 일정 식별자", example = "task-1") String tempId,
        @Schema(description = "일정 제목", example = "폐업 신고서 제출") String title,
        @Schema(description = "일정 시작 날짜", example = "2026-08-20") LocalDate startDate,
        @Schema(description = "일정 시작 시각", example = "10:00:00") LocalTime startTime,
        @Schema(description = "일정 종료 날짜", example = "2026-08-20") LocalDate endDate,
        @Schema(description = "일정 종료 시각", example = "22:00:00") LocalTime endTime,
        @Schema(description = "일정 메모", example = "관할 세무서 방문 필요") String memo) {}
