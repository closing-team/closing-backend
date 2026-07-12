package com.closing.closing.domain.task.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode {
    TASK_TITLE_BLANK(HttpStatus.BAD_REQUEST, "TASK400", "일정 제목이 비어있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
