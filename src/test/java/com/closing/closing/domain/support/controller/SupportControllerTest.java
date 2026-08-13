package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.SupportService;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SupportControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private SupportService supportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SupportController supportController = new SupportController(supportService);

        mockMvc = MockMvcBuilders.standaloneSetup(supportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new TestPrincipalResolver(USER_ID))
                .build();
    }

    @Test
    @DisplayName("인증된 사용자의 ID로 지원정보 목록을 조회한다")
    void getSupports_Success_WithAuthenticatedUser() throws Exception {
        SupportResDTO.SupportListDTO response = SupportResDTO.SupportListDTO.builder()
                .supports(List.of())
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(null)
                        .hasNext(false)
                        .build())
                .build();
        when(supportService.getSupports(
                USER_ID, "POPULAR", null, "20"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/supports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supports").isEmpty());

        verify(supportService).getSupports(
                USER_ID, "POPULAR", null, "20");
    }

    @Test
    @DisplayName("최초 동기화 중인 빈 DB에서는 SUPPORT503을 반환한다")
    void getSupports_Fail_WhenInitialSyncIsInProgress() throws Exception {
        when(supportService.getSupports(
                USER_ID, "POPULAR", null, "20"))
                .thenThrow(new CustomException(
                        ErrorCode.SUPPORT_SYNC_IN_PROGRESS));

        mockMvc.perform(get("/api/v1/supports"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("SUPPORT503"))
                .andExpect(jsonPath("$.message").value(
                        "지원정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요."));
    }
}
