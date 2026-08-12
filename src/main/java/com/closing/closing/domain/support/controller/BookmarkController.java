package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.BookmarkReqDTO;
import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.service.BookmarkService;
import com.closing.closing.domain.support.enums.BookmarkSort;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "08. Bookmark", description = "지원정보 북마크 API")
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 목록 조회", description = "등록한 지원정보 북마크 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<BookmarkResDTO.BookmarkListDTO> getBookmarks(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "북마크 정렬 기준",
                    schema = @Schema(
                            implementation = BookmarkSort.class,
                            defaultValue = "LATEST"))
            @RequestParam(value = "sort", defaultValue = "LATEST") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "20") String size
    ) {
        return ApiResponse.onSuccess(
                bookmarkService.getBookmarks(userId, sort, cursor, size));
    }

    @Operation(summary = "북마크 추가", description = "지원정보를 북마크에 추가합니다.")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BookmarkResDTO.BookmarkCreateDTO> createBookmark(
            @AuthenticationPrincipal Long userId,
            @RequestBody BookmarkReqDTO.BookmarkCreateDTO request
    ) {
        return ApiResponse.onSuccess(
                bookmarkService.createBookmark(userId, request.supportId()));
    }

    @Operation(summary = "북마크 삭제", description = "등록한 지원정보 북마크를 삭제합니다.")
    @DeleteMapping("/{supportId}")
    public ApiResponse<Void> deleteBookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable("supportId") Long supportId
    ) {
        bookmarkService.deleteBookmark(userId, supportId);
        return ApiResponse.onSuccess(null);
    }
}
