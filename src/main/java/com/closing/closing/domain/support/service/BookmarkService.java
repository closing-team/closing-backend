package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.entity.Bookmark;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.repository.BookmarkRepository;
import com.closing.closing.domain.support.repository.SupportRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final LocalDate LAST_END_DATE = LocalDate.of(9999, 12, 31);

    private final BookmarkRepository bookmarkRepository;
    private final SupportRepository supportRepository;
    private final EntityManager entityManager;

    @Transactional
    public BookmarkResDTO.BookmarkCreateDTO createBookmark(
            Long userId,
            Long supportId) {
        if (supportId == null || supportId <= 0) {
            throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
        }

        SupportInfo supportInfo = supportRepository.findById(supportId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SUPPORT_NOT_FOUND));

        if (bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId)) {
            throw new CustomException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        User user = entityManager.getReference(User.class, userId);
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .supportInfo(supportInfo)
                .build();

        try {
            Bookmark savedBookmark = bookmarkRepository.saveAndFlush(bookmark);
            return BookmarkResDTO.BookmarkCreateDTO.from(savedBookmark);
        } catch (DataIntegrityViolationException exception) {
            // 사전 중복 확인 이후 동시에 저장된 경우에도 UNIQUE 제약으로 중복을 차단한다.
            if (isUniqueConstraintViolation(exception)) {
                throw new CustomException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
            }
            throw exception;
        }
    }

    @Transactional
    public void deleteBookmark(Long userId, Long supportId) {
        Bookmark bookmark = bookmarkRepository.findByUser_IdAndSupportInfo_Id(userId, supportId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

    public BookmarkResDTO.BookmarkListDTO getBookmarks(
            Long userId,
            String sortValue,
            String cursorValue,
            String sizeValue) {
        BookmarkSort sort = BookmarkSort.from(sortValue);
        int size = parseSize(sizeValue);
        BookmarkCursor cursor = parseCursor(sort, cursorValue);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Bookmark> result = findBookmarks(userId, sort, cursor, pageable);
        boolean hasNext = result.size() > size;
        List<Bookmark> bookmarks = hasNext ? result.subList(0, size) : result;

        List<SupportResDTO.SupportSummaryDTO> bookmarkDTOs = bookmarks.stream()
                .map(bookmark -> SupportResDTO.SupportSummaryDTO.from(
                        bookmark.getSupportInfo(), true))
                .toList();

        String nextCursor = hasNext
                ? createCursor(sort, bookmarks.get(bookmarks.size() - 1))
                : null;

        return BookmarkResDTO.BookmarkListDTO.builder()
                .bookmarks(bookmarkDTOs)
                .page(SupportResDTO.PageDTO.builder()
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    private List<Bookmark> findBookmarks(
            Long userId,
            BookmarkSort sort,
            BookmarkCursor cursor,
            Pageable pageable) {
        return switch (sort) {
            case POPULAR -> bookmarkRepository.findAllByPopular(
                    userId, cursor.viewCount(), cursor.bookmarkId(), pageable);
            case LATEST -> bookmarkRepository.findAllByLatest(
                    userId, cursor.bookmarkId(), pageable);
            case DEADLINE -> bookmarkRepository.findAllByDeadline(
                    userId, cursor.applyEndDate(), cursor.bookmarkId(),
                    LAST_END_DATE, pageable);
        };
    }

    private int parseSize(String value) {
        try {
            int size = Integer.parseInt(value);
            if (size < 1 || size > MAX_PAGE_SIZE) {
                throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
            }
            return size;
        } catch (NumberFormatException exception) {
            throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
        }
    }

    private BookmarkCursor parseCursor(BookmarkSort sort, String value) {
        if (value == null || value.isBlank()) {
            return BookmarkCursor.initial();
        }

        try {
            if (sort == BookmarkSort.LATEST) {
                long bookmarkId = parseBookmarkId(value);
                return BookmarkCursor.latest(bookmarkId);
            }

            int separatorIndex = value.lastIndexOf('_');
            if (separatorIndex <= 0 || separatorIndex == value.length() - 1) {
                throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
            }

            String sortCursor = value.substring(0, separatorIndex);
            long bookmarkId = parseBookmarkId(value.substring(separatorIndex + 1));

            if (sort == BookmarkSort.POPULAR) {
                int viewCount = Integer.parseInt(sortCursor);
                if (viewCount < 0) {
                    throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
                }
                return BookmarkCursor.popular(bookmarkId, viewCount);
            }

            return BookmarkCursor.deadline(bookmarkId, LocalDate.parse(sortCursor));
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
        }
    }

    private long parseBookmarkId(String value) {
        long bookmarkId = Long.parseLong(value);
        if (bookmarkId <= 0) {
            throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
        }
        return bookmarkId;
    }

    private String createCursor(BookmarkSort sort, Bookmark bookmark) {
        return switch (sort) {
            case POPULAR -> bookmark.getSupportInfo().getViewCount()
                    + "_" + bookmark.getId();
            case LATEST -> String.valueOf(bookmark.getId());
            case DEADLINE -> (bookmark.getSupportInfo().getApplyEndDate() == null
                    ? LAST_END_DATE
                    : bookmark.getSupportInfo().getApplyEndDate())
                    + "_" + bookmark.getId();
        };
    }

    private enum BookmarkSort {
        POPULAR,
        LATEST,
        DEADLINE;

        private static BookmarkSort from(String value) {
            try {
                return BookmarkSort.valueOf(value);
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
            }
        }
    }

    private record BookmarkCursor(
            Long bookmarkId,
            Integer viewCount,
            LocalDate applyEndDate
    ) {
        private static BookmarkCursor initial() {
            return new BookmarkCursor(null, null, null);
        }

        private static BookmarkCursor popular(long bookmarkId, int viewCount) {
            return new BookmarkCursor(bookmarkId, viewCount, null);
        }

        private static BookmarkCursor latest(long bookmarkId) {
            return new BookmarkCursor(bookmarkId, null, null);
        }

        private static BookmarkCursor deadline(long bookmarkId, LocalDate applyEndDate) {
            return new BookmarkCursor(bookmarkId, null, applyEndDate);
        }
    }

    private boolean isUniqueConstraintViolation(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
