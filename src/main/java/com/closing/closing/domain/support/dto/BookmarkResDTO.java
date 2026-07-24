package com.closing.closing.domain.support.dto;

import com.closing.closing.domain.support.entity.Bookmark;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class BookmarkResDTO {

    private static final ZoneOffset KOREA_OFFSET = ZoneOffset.ofHours(9);

    @Builder
    public record BookmarkCreateDTO(
            Long bookmarkId,
            Long supportId,
            OffsetDateTime createdAt
    ) {
        public static BookmarkCreateDTO from(Bookmark bookmark) {
            return BookmarkCreateDTO.builder()
                    .bookmarkId(bookmark.getId())
                    .supportId(bookmark.getSupportInfo().getId())
                    .createdAt(bookmark.getCreatedAt().atOffset(KOREA_OFFSET))
                    .build();
        }
    }

    @Builder
    public record BookmarkListDTO(
            List<SupportResDTO.SupportSummaryDTO> bookmarks,
            SupportResDTO.PageDTO page
    ) {
    }
}
