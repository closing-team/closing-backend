package com.closing.closing.domain.task.exception;

import com.closing.closing.domain.task.exception.code.TaskErrorCode;
import lombok.Getter;

@Getter
public class TaskException extends RuntimeException {

    private final TaskErrorCode taskErrorCode;

    public TaskException(TaskErrorCode taskErrorCode) {
        super(taskErrorCode.getMessage());
        this.taskErrorCode = taskErrorCode;
    }
}
