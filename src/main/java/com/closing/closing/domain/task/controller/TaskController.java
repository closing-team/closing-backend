package com.closing.closing.domain.task.controller;

import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.service.TaskService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task", description = "캘린더 일정 API")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 일정 추가
     */
    @Operation(summary = "일정 추가", description = "할일을 수동으로 추가합니다.")
    @PostMapping
    public ApiResponse<TaskResDTO.CreateTaskResultDTO> createTask(
            @Valid @RequestBody TaskReqDTO.CreateTaskDTO request
    ) {
        return ApiResponse.onSuccess(taskService.createTask(request));
    }

    /**
     * 일정 수정
     */
    @Operation(summary = "일정 수정", description = "할일을 수정합니다.")
    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResDTO.UpdateTaskResultDTO> updateTask(
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody TaskReqDTO.UpdateTaskDTO request
    ) {
        return ApiResponse.onSuccess(taskService.updateTask(taskId, request));
    }

    /**
     * 일정 삭제
     */
    @Operation(summary = "일정 삭제", description = "일정(할일)을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable("taskId") Long taskId
    ) {
        taskService.deleteTask(taskId);
        return ApiResponse.onSuccess(null);
    }
}
