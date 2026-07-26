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
import org.springframework.http.HttpHeaders;
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

    @Mock
    private SupportService supportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SupportController supportController = new SupportController(supportService);

        mockMvc = MockMvcBuilders.standaloneSetup(supportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("인증 헤더가 없으면 Support API 접근에 실패한다")
    void getSupports_Fail_WhenAuthorizationHeaderMissing() throws Exception {
        when(supportService.getSupports("POPULAR", null, "20", null))
                .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/v1/supports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(supportService).getSupports("POPULAR", null, "20", null);
    }

    @Test
    @DisplayName("Authorization 헤더를 전달해 지원정보 목록을 조회한다")
    void getSupports_Success_WithAuthorizationHeader() throws Exception {
        SupportResDTO.SupportListDTO response = SupportResDTO.SupportListDTO.builder()
                .supports(List.of())
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(null)
                        .hasNext(false)
                        .build())
                .build();
        when(supportService.getSupports(
                "POPULAR", null, "20", "Bearer access-token"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/supports")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supports").isEmpty());

        verify(supportService).getSupports(
                "POPULAR", null, "20", "Bearer access-token");
    }
}
