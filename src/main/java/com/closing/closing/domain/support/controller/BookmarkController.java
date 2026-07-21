package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.BookmarkReqDTO;
import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.service.BookmarkService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookmark", description = "지원정보 북마크 API")
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "지원정보를 북마크에 추가합니다.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BookmarkResDTO.BookmarkCreateDTO> createBookmark(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @RequestBody BookmarkReqDTO.BookmarkCreateDTO request
    ) {
        // TODO: 인증 연동 후 Authorization 토큰의 사용자 ID를 사용한다.
        Long userId = 1L;

        return ApiResponse.onSuccess(
                bookmarkService.createBookmark(userId, request.supportId()));
    }
}
