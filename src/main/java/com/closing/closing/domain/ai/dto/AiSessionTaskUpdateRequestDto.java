package com.closing.closing.domain.ai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiSessionTaskUpdateRequestDto(
        String title,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String memo) {}
