package com.closing.closing.domain.ai.controller;

import com.closing.closing.domain.ai.dto.AiGeneratedTaskDto;
import com.closing.closing.domain.ai.dto.AiSessionDetailResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionMessageRequestDto;
import com.closing.closing.domain.ai.dto.AiSessionMessageResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionRequestDto;
import com.closing.closing.domain.ai.dto.AiSessionResponseDto;
import com.closing.closing.domain.ai.dto.AiSessionTaskUpdateRequestDto;
import com.closing.closing.domain.ai.service.AiSessionService;
import com.closing.closing.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/sessions")
@RequiredArgsConstructor
public class AiSessionController {

    private final AiSessionService aiSessionService;

    //세션 시작
    @PostMapping
    public ApiResponse<AiSessionResponseDto> createSession(@RequestBody AiSessionRequestDto request) {
        return ApiResponse.onSuccess(aiSessionService.createSession(request));
    }

    //세션 조회
    @GetMapping("/{sessionId}")
    public ApiResponse<AiSessionDetailResponseDto> getSession(@PathVariable String sessionId) {
        return ApiResponse.onSuccess(aiSessionService.getSession(sessionId));
    }

    //메시지 전송
    @PostMapping("/{sessionId}/messages")
    public ApiResponse<AiSessionMessageResponseDto> sendMessage(
            @PathVariable String sessionId, @RequestBody AiSessionMessageRequestDto request) {
        return ApiResponse.onSuccess(aiSessionService.sendMessage(sessionId, request.message()));
    }

    //임시 일정 수정
    @PatchMapping("/{sessionId}/tasks/{tempId}")
    public ApiResponse<AiGeneratedTaskDto> updateTask(
            @PathVariable String sessionId,
            @PathVariable String tempId,
            @RequestBody AiSessionTaskUpdateRequestDto request) {
        return ApiResponse.onSuccess(aiSessionService.updateTask(sessionId, tempId, request));
    }

    //임시 일정 삭제
    @DeleteMapping("/{sessionId}/tasks/{tempId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable String sessionId,
            @PathVariable String tempId) {
        aiSessionService.deleteTask(sessionId, tempId);
        return ApiResponse.onSuccess(null);
    }
}
