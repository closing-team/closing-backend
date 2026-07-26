package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.BookmarkService;
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
class BookmarkControllerTest {

    @Mock
    private BookmarkService bookmarkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BookmarkController bookmarkController = new BookmarkController(bookmarkService);

        mockMvc = MockMvcBuilders.standaloneSetup(bookmarkController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("인증 헤더가 없으면 Bookmark API 접근에 실패한다")
    void getBookmarks_Fail_WhenAuthorizationHeaderMissing() throws Exception {
        when(bookmarkService.getBookmarks(null, "LATEST", null, "20"))
                .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/api/v1/bookmarks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401"));

        verify(bookmarkService).getBookmarks(null, "LATEST", null, "20");
    }

    @Test
    @DisplayName("Authorization 헤더를 전달해 사용자의 북마크 목록을 조회한다")
    void getBookmarks_Success_WithAuthorizationHeader() throws Exception {
        BookmarkResDTO.BookmarkListDTO response = BookmarkResDTO.BookmarkListDTO.builder()
                .bookmarks(List.of())
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(null)
                        .hasNext(false)
                        .build())
                .build();
        when(bookmarkService.getBookmarks(
                "Bearer access-token", "LATEST", null, "20"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/bookmarks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookmarks").isEmpty());

        verify(bookmarkService).getBookmarks(
                "Bearer access-token", "LATEST", null, "20");
    }
}
