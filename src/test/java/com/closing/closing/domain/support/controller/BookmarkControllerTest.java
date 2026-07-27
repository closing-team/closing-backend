package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.BookmarkService;
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
class BookmarkControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private BookmarkService bookmarkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BookmarkController bookmarkController = new BookmarkController(bookmarkService);

        mockMvc = MockMvcBuilders.standaloneSetup(bookmarkController)
                .setCustomArgumentResolvers(
                        new TestPrincipalResolver(USER_ID))
                .build();
    }

    @Test
    @DisplayName("인증된 사용자의 ID로 북마크 목록을 조회한다")
    void getBookmarks_Success_WithAuthenticatedUser() throws Exception {
        BookmarkResDTO.BookmarkListDTO response = BookmarkResDTO.BookmarkListDTO.builder()
                .bookmarks(List.of())
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(null)
                        .hasNext(false)
                        .build())
                .build();
        when(bookmarkService.getBookmarks(
                USER_ID, "LATEST", null, "20"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookmarks").isEmpty());

        verify(bookmarkService).getBookmarks(
                USER_ID, "LATEST", null, "20");
    }
}
