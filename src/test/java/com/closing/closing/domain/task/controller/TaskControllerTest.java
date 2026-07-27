package com.closing.closing.domain.task.controller;

import com.closing.closing.domain.task.dto.TaskResDTO;
import com.closing.closing.domain.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskController taskController = new TaskController(taskService);

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
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

        when(taskService.getHome(USER_ID, yearMonth)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/home")
                        .param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalCount").value(0));

        verify(taskService).getHome(USER_ID, yearMonth);
    }

    private static class AuthenticationPrincipalArgumentResolver
            implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return USER_ID;
        }
    }
}
