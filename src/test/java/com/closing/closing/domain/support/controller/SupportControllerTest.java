package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.SupportService;
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
}
