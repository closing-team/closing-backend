package com.closing.closing.domain.ai.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiGeneratedTaskDto(
        String tempId,
        String title,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String memo) {}
