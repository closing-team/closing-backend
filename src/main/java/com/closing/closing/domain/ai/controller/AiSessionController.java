package com.closing.closing.domain.ai.controller;

import com.closing.closing.domain.ai.dto.AiSessionRequestDto;
import com.closing.closing.domain.ai.dto.AiSessionResponseDto;
import com.closing.closing.domain.ai.service.AiSessionService;
import com.closing.closing.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/sessions")
@RequiredArgsConstructor
public class AiSessionController {

    private final AiSessionService aiSessionService;

    @PostMapping
    public ApiResponse<AiSessionResponseDto> createSession(@RequestBody AiSessionRequestDto request) {
        return ApiResponse.onSuccess(aiSessionService.createSession(request));
    }
}
