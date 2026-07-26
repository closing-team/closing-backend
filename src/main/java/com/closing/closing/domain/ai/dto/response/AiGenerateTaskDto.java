package com.closing.closing.domain.ai.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiGenerateTaskDto(
        String title,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String memo) {}
