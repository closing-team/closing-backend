package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.BookmarkResDTO;
import com.closing.closing.domain.support.entity.Bookmark;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import com.closing.closing.domain.support.repository.BookmarkRepository;
import com.closing.closing.domain.support.repository.SupportRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.global.entity.BaseCreatedEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

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
                bookmarkService.createBookmark(userId, supportId);

        // then
        assertEquals(bookmarkId, result.bookmarkId());
        assertEquals(supportId, result.supportId());
        assertEquals(createdAt.atOffset(ZoneOffset.ofHours(9)), result.createdAt());
        verify(bookmarkRepository).saveAndFlush(any(Bookmark.class));
    }

    @Test
    @DisplayName("존재하지 않는 지원정보 북마크 시 SUPPORT404 예외 발생")
    void createBookmark_Fail_WhenSupportNotFound() {
        // given
        Long userId = 1L;
        Long supportId = 999L;
        when(supportRepository.findById(supportId)).thenReturn(Optional.empty());

        // when & then
        SupportException exception = assertThrows(SupportException.class,
                () -> bookmarkService.createBookmark(userId, supportId));

        assertEquals(
                SupportErrorCode.SUPPORT_NOT_FOUND,
                exception.getSupportErrorCode());
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
        SupportException exception = assertThrows(SupportException.class,
                () -> bookmarkService.createBookmark(userId, supportId));

        assertEquals(
                SupportErrorCode.BOOKMARK_ALREADY_EXISTS,
                exception.getSupportErrorCode());
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
        SupportException exception = assertThrows(SupportException.class,
                () -> bookmarkService.createBookmark(userId, supportId));

        assertEquals(
                SupportErrorCode.BOOKMARK_ALREADY_EXISTS,
                exception.getSupportErrorCode());
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
                () -> bookmarkService.createBookmark(userId, supportId));

        assertEquals(integrityException, exception);
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
