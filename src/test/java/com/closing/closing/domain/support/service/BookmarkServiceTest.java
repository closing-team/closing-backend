package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.entity.Bookmark;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.closing.closing.domain.support.repository.BookmarkRepository;
import com.closing.closing.domain.support.repository.SupportRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.entity.BaseCreatedEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private SupportRepository supportRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("지원정보 북마크 추가 성공")
    void createBookmark_Success() throws Exception {
        // given
        Long userId = 1L;
        Long supportId = 1L;
        Long bookmarkId = 10L;
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 4, 13, 0);
        SupportInfo supportInfo = createSupport(supportId);
        User user = User.builder()
                .kakaoId("kakao-1")
                .nickname("클로징")
                .build();

        when(supportRepository.findById(supportId))
                .thenReturn(Optional.of(supportInfo));
        when(bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(false);
        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(bookmarkRepository.saveAndFlush(any(Bookmark.class)))
                .thenAnswer(invocation -> {
                    Bookmark bookmark = invocation.getArgument(0);
                    setField(bookmark, "id", bookmarkId);
                    setCreatedAt(bookmark, createdAt);
                    return bookmark;
                });

        // when
        BookmarkResDTO.BookmarkCreateDTO result =
                bookmarkService.createBookmark(USER_ID, supportId);

        // then
        assertEquals(bookmarkId, result.bookmarkId());
        assertEquals(supportId, result.supportId());
        assertEquals(createdAt.atOffset(ZoneOffset.ofHours(9)), result.createdAt());
        verify(bookmarkRepository).saveAndFlush(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 추가 시 지원정보 ID가 null이면 BOOKMARK400 예외 발생")
    void createBookmark_Fail_WhenSupportIdIsNull() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.createBookmark(USER_ID, null));

        assertEquals(
                ErrorCode.BOOKMARK_INVALID_QUERY,
                exception.getErrorCode());
        assertEquals("BOOKMARK400", exception.getErrorCode().getCode());
        verify(supportRepository, never()).findById(any());
    }

    @Test
    @DisplayName("북마크 추가 시 지원정보 ID가 양수가 아니면 BOOKMARK400 예외 발생")
    void createBookmark_Fail_WhenSupportIdIsNotPositive() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.createBookmark(USER_ID, 0L));

        assertEquals(
                ErrorCode.BOOKMARK_INVALID_QUERY,
                exception.getErrorCode());
        verify(supportRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 지원정보 북마크 시 SUPPORT404 예외 발생")
    void createBookmark_Fail_WhenSupportNotFound() {
        // given
        Long userId = 1L;
        Long supportId = 999L;
        when(supportRepository.findById(supportId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.createBookmark(USER_ID, supportId));

        assertEquals(
                ErrorCode.SUPPORT_NOT_FOUND,
                exception.getErrorCode());
        verify(bookmarkRepository, never())
                .existsByUser_IdAndSupportInfo_Id(userId, supportId);
    }

    @Test
    @DisplayName("이미 등록된 북마크이면 BOOKMARK409 예외 발생")
    void createBookmark_Fail_WhenBookmarkAlreadyExists() throws Exception {
        // given
        Long userId = 1L;
        Long supportId = 1L;
        when(supportRepository.findById(supportId))
                .thenReturn(Optional.of(createSupport(supportId)));
        when(bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.createBookmark(USER_ID, supportId));

        assertEquals(
                ErrorCode.BOOKMARK_ALREADY_EXISTS,
                exception.getErrorCode());
        verify(bookmarkRepository, never()).saveAndFlush(any(Bookmark.class));
    }

    @Test
    @DisplayName("동시 저장으로 UNIQUE 제약이 충돌하면 BOOKMARK409 예외 발생")
    void createBookmark_Fail_WhenUniqueConstraintConflicts() throws Exception {
        // given
        Long userId = 1L;
        Long supportId = 1L;
        User user = User.builder()
                .kakaoId("kakao-1")
                .nickname("클로징")
                .build();
        when(supportRepository.findById(supportId))
                .thenReturn(Optional.of(createSupport(supportId)));
        when(bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(false);
        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(bookmarkRepository.saveAndFlush(any(Bookmark.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "unique constraint",
                        new SQLException("unique violation", "23505")));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.createBookmark(USER_ID, supportId));

        assertEquals(
                ErrorCode.BOOKMARK_ALREADY_EXISTS,
                exception.getErrorCode());
    }

    @Test
    @DisplayName("UNIQUE 외 무결성 오류는 BOOKMARK409로 변환하지 않음")
    void createBookmark_Fail_WhenOtherIntegrityConstraintConflicts() throws Exception {
        // given
        Long userId = 1L;
        Long supportId = 1L;
        User user = User.builder()
                .kakaoId("kakao-1")
                .nickname("클로징")
                .build();
        DataIntegrityViolationException integrityException =
                new DataIntegrityViolationException(
                        "foreign key constraint",
                        new SQLException("foreign key violation", "23503"));
        when(supportRepository.findById(supportId))
                .thenReturn(Optional.of(createSupport(supportId)));
        when(bookmarkRepository.existsByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(false);
        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(bookmarkRepository.saveAndFlush(any(Bookmark.class)))
                .thenThrow(integrityException);

        // when & then
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> bookmarkService.createBookmark(USER_ID, supportId));

        assertEquals(integrityException, exception);
    }

    @Test
    @DisplayName("지원정보 북마크 삭제 성공")
    void deleteBookmark_Success() throws Exception {
        // given
        Long userId = 1L;
        Long supportId = 1L;
        Bookmark bookmark = Bookmark.builder()
                .user(User.builder()
                        .kakaoId("kakao-1")
                        .nickname("클로징")
                        .build())
                .supportInfo(createSupport(supportId))
                .build();
        when(bookmarkRepository.findByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(Optional.of(bookmark));

        // when
        bookmarkService.deleteBookmark(USER_ID, supportId);

        // then
        verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    @DisplayName("본인의 북마크를 찾을 수 없으면 BOOKMARK404 예외 발생")
    void deleteBookmark_Fail_WhenBookmarkNotFound() {
        // given
        Long userId = 1L;
        Long supportId = 999L;
        when(bookmarkRepository.findByUser_IdAndSupportInfo_Id(userId, supportId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.deleteBookmark(USER_ID, supportId));

        assertEquals(
                ErrorCode.BOOKMARK_NOT_FOUND,
                exception.getErrorCode());
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 목록 인기순 조회 성공")
    void getBookmarks_Success_OrderByPopular() throws Exception {
        // given
        Long userId = 1L;
        Bookmark firstBookmark = createBookmark(
                10L, 1L, "첫 번째 공고", 1520,
                LocalDate.of(2026, 12, 31));
        Bookmark secondBookmark = createBookmark(
                9L, 2L, "두 번째 공고", 1000,
                LocalDate.of(2026, 11, 30));
        Bookmark nextBookmark = createBookmark(
                8L, 3L, "다음 페이지 공고", 500,
                LocalDate.of(2026, 10, 31));
        when(bookmarkRepository.findAllByPopular(
                userId, null, null, PageRequest.of(0, 3)))
                .thenReturn(List.of(firstBookmark, secondBookmark, nextBookmark));

        // when
        BookmarkResDTO.BookmarkListDTO result = bookmarkService.getBookmarks(
                USER_ID, "POPULAR", null, "2");

        // then
        assertEquals(2, result.bookmarks().size());
        assertEquals(1L, result.bookmarks().get(0).supportId());
        assertEquals("첫 번째 공고", result.bookmarks().get(0).title());
        assertTrue(result.bookmarks().get(0).isBookmarked());
        assertTrue(result.page().hasNext());
        assertEquals("1000_9", result.page().nextCursor());
    }

    @Test
    @DisplayName("북마크 목록의 다음 페이지가 없으면 커서를 반환하지 않음")
    void getBookmarks_Success_WithoutNextPage() throws Exception {
        // given
        Long userId = 1L;
        Bookmark bookmark = createBookmark(
                10L, 1L, "지원 공고", 100,
                LocalDate.of(2026, 12, 31));
        when(bookmarkRepository.findAllByLatest(
                userId, null, PageRequest.of(0, 21)))
                .thenReturn(List.of(bookmark));

        // when
        BookmarkResDTO.BookmarkListDTO result = bookmarkService.getBookmarks(
                USER_ID, "LATEST", null, "20");

        // then
        assertEquals(1, result.bookmarks().size());
        assertFalse(result.page().hasNext());
        assertNull(result.page().nextCursor());
    }

    @Test
    @DisplayName("북마크 등록 최신순 커서를 기준으로 다음 목록 조회")
    void getBookmarks_Success_WithLatestCursor() {
        // given
        Long userId = 1L;
        when(bookmarkRepository.findAllByLatest(
                userId, 10L, PageRequest.of(0, 21)))
                .thenReturn(List.of());

        // when
        bookmarkService.getBookmarks(
                USER_ID, "LATEST", "10", "20");

        // then
        verify(bookmarkRepository).findAllByLatest(
                userId, 10L, PageRequest.of(0, 21));
    }

    @Test
    @DisplayName("마감일순 커서를 기준으로 다음 북마크 목록 조회")
    void getBookmarks_Success_WithDeadlineCursor() {
        // given
        Long userId = 1L;
        LocalDate cursorEndDate = LocalDate.of(2026, 12, 31);
        when(bookmarkRepository.findAllByDeadline(
                userId,
                cursorEndDate,
                10L,
                LocalDate.of(9999, 12, 31),
                PageRequest.of(0, 21)))
                .thenReturn(List.of());

        // when
        bookmarkService.getBookmarks(
                USER_ID, "DEADLINE", "2026-12-31_10", "20");

        // then
        verify(bookmarkRepository).findAllByDeadline(
                userId,
                cursorEndDate,
                10L,
                LocalDate.of(9999, 12, 31),
                PageRequest.of(0, 21));
    }

    @Test
    @DisplayName("지원하지 않는 북마크 정렬값이면 BOOKMARK400 예외 발생")
    void getBookmarks_Fail_WhenSortIsInvalid() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.getBookmarks(
                        USER_ID, "OLDEST", null, "20"));

        assertEquals(
                ErrorCode.BOOKMARK_INVALID_QUERY,
                exception.getErrorCode());
    }

    @Test
    @DisplayName("북마크 커서 형식이 잘못되면 BOOKMARK400 예외 발생")
    void getBookmarks_Fail_WhenCursorIsInvalid() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.getBookmarks(
                        USER_ID, "POPULAR", "invalid", "20"));

        assertEquals(
                ErrorCode.BOOKMARK_INVALID_QUERY,
                exception.getErrorCode());
    }

    @Test
    @DisplayName("북마크 페이지 크기가 허용 범위를 벗어나면 BOOKMARK400 예외 발생")
    void getBookmarks_Fail_WhenSizeIsOutOfRange() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bookmarkService.getBookmarks(
                        USER_ID, "LATEST", null, "101"));

        assertEquals(
                ErrorCode.BOOKMARK_INVALID_QUERY,
                exception.getErrorCode());
    }

    private Bookmark createBookmark(
            Long bookmarkId,
            Long supportId,
            String title,
            int viewCount,
            LocalDate applyEndDate) throws Exception {
        SupportInfo supportInfo = SupportInfo.builder()
                .organizationName("소상공인시장진흥공단")
                .title(title)
                .applyStartDate(LocalDate.of(2026, 1, 1))
                .applyEndDate(applyEndDate)
                .applicationPeriod("2026-01-01 ~ " + applyEndDate)
                .status(SupportStatus.ONGOING)
                .viewCount(viewCount)
                .build();
        setField(supportInfo, "id", supportId);

        Bookmark bookmark = Bookmark.builder()
                .user(User.builder()
                        .kakaoId("kakao-1")
                        .nickname("클로징")
                        .build())
                .supportInfo(supportInfo)
                .build();
        setField(bookmark, "id", bookmarkId);
        return bookmark;
    }

    private SupportInfo createSupport(Long supportId) throws Exception {
        SupportInfo supportInfo = SupportInfo.builder()
                .organizationName("소상공인시장진흥공단")
                .title("희망리턴패키지")
                .build();
        setField(supportInfo, "id", supportId);
        return supportInfo;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setCreatedAt(Bookmark bookmark, LocalDateTime createdAt) throws Exception {
        Field field = BaseCreatedEntity.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(bookmark, createdAt);
    }
}
