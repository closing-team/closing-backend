package com.closing.closing.domain.task.exception;

import com.closing.closing.domain.task.exception.code.TaskErrorCode;
import com.closing.closing.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskExceptionHandler {

    @ExceptionHandler(TaskException.class)
    public ResponseEntity<ApiResponse<Void>> handleTaskException(TaskException e) {
        TaskErrorCode errorCode = e.getTaskErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
