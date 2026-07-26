package com.closing.closing.domain.task.controller;

import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.service.TaskService;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskController taskController = new TaskController(taskService);

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("인증 헤더가 없으면 Task API 접근에 실패한다")
    void getHome_Fail_WhenAuthorizationHeaderMissing() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026, 7);
        when(taskService.getHome(null, yearMonth))
                .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/v1/tasks/home")
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(taskService).getHome(null, yearMonth);
    }

    @Test
    @DisplayName("인증된 사용자의 ID로 홈 화면을 조회한다")
    void getHome_Success_WithAuthenticatedUser() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026, 7);
        TaskResDTO.HomeDTO response = TaskResDTO.HomeDTO.builder()
                .summary(TaskResDTO.SummaryDTO.builder()
                        .totalCount(0)
                        .completedCount(0)
                        .progressRate(0.0)
                        .build())
                .calendar(List.of())
                .build();

        when(taskService.getHome("Bearer access-token", yearMonth)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/home")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalCount").value(0));

        verify(taskService).getHome("Bearer access-token", yearMonth);
    }
}
