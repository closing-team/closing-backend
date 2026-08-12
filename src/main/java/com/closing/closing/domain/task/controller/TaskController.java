package com.closing.closing.domain.task.controller;

import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.service.TaskService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Tag(name = "06. Task", description = "캘린더 일정 API")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "일정 추가", description = "일정을 수동으로 추가합니다.")
    @PostMapping
    public ApiResponse<TaskResDTO.CreateTaskResultDTO> createTask(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TaskReqDTO.CreateTaskDTO request
    ) {
        return ApiResponse.onSuccess(taskService.createTask(userId, request));
    }

    @Operation(summary = "일정 상세 조회", description = "일정의 상세 정보를 조회합니다.")
    @GetMapping("/{taskId}")
    public ApiResponse<TaskResDTO.TaskDetailDTO> getTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable("taskId") Long taskId
    ) {
        return ApiResponse.onSuccess(taskService.getTask(userId, taskId));
    }

    @Operation(
            summary = "일정 수정",
            description = "일정을 부분 수정합니다. null이 아닌 필드만 반영되며, 누락되었거나 null인 필드는 기존 값이 유지됩니다."
    )
    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResDTO.UpdateTaskResultDTO> updateTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody TaskReqDTO.UpdateTaskDTO request
    ) {
        return ApiResponse.onSuccess(
                taskService.updateTask(userId, taskId, request));
    }

    @Operation(summary = "홈 화면 전체 조회", description = "전체 일정의 진행도와 해당 월의 일정 목록을 조회합니다.")
    @GetMapping("/home")
    public ApiResponse<TaskResDTO.HomeDTO> getHome(
            @AuthenticationPrincipal Long userId,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        return ApiResponse.onSuccess(taskService.getHome(userId, yearMonth));
    }

    @Operation(summary = "일정 완료 처리", description = "일정의 완료 상태를 변경합니다.")
    @PatchMapping("/{taskId}/complete")
    public ApiResponse<TaskResDTO.CompleteTaskResultDTO> completeTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody TaskReqDTO.CompleteTaskDTO request
    ) {
        return ApiResponse.onSuccess(
                taskService.completeTask(userId, taskId, request));
    }

    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable("taskId") Long taskId
    ) {
        taskService.deleteTask(userId, taskId);
        return ApiResponse.onSuccess(null);
    }
}
