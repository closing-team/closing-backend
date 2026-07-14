package com.closing.closing.domain.task.controller;

import com.closing.closing.domain.task.dto.TaskReqDTO;
import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.service.TaskService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task", description = "캘린더 일정 API")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * HOME003 - 캘린더 일정 추가
     */
    @Operation(summary = "일정 추가", description = "캘린더 일정(할일)을 수동으로 추가합니다.")
    @PostMapping
    public ApiResponse<TaskResDTO.CreateTaskResultDTO> createTask(
            @RequestBody TaskReqDTO.CreateTaskDTO request
    ) {
        return ApiResponse.onSuccess(taskService.createTask(request));
    }
}
