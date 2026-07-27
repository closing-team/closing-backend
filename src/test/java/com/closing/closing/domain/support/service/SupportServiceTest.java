package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.closing.closing.domain.support.repository.BookmarkRepository;
import com.closing.closing.domain.support.repository.SupportRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private SupportRepository supportRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private SupportService supportService;

    @Test
    @DisplayName("지원정보 상세 조회 성공 및 조회수 증가")
    void getSupport_Success_IncreaseViewCount() throws Exception {
        // given
        Long supportId = 1L;
        SupportInfo supportInfo = SupportInfo.builder()
                .organizationName("소상공인시장진흥공단")
                .title("2026년 희망리턴패키지 원스톱 폐업지원")
                .content("폐업 소상공인의 안전한 폐업 및 재기를 지원합니다.")
                .applyStartDate(LocalDate.of(2026, 1, 1))
                .applicationPeriod("예산 소진시까지")
                .applicationUrl("https://www.bizinfo.go.kr/support/1")
                .status(SupportStatus.ONGOING)
                .viewCount(1521)
                .build();
        setField(supportInfo, "id", supportId);

        when(supportRepository.increaseViewCount(supportId)).thenReturn(1);
        when(supportRepository.findById(supportId))
                .thenReturn(Optional.of(supportInfo));
        when(bookmarkRepository.existsByUser_IdAndSupportInfo_Id(USER_ID, supportId))
                .thenReturn(true);

        // when
        SupportResDTO.SupportDetailDTO result =
                supportService.getSupport(USER_ID, supportId);

        // then
        assertEquals(supportId, result.supportId());
        assertEquals("소상공인시장진흥공단", result.organizationName());
        assertEquals("예산 소진시까지", result.applicationPeriod());
        assertEquals("https://www.bizinfo.go.kr/support/1", result.externalUrl());
        assertEquals(1521, result.viewCount());
        assertTrue(result.isBookmarked());
        var orderedRepository = inOrder(supportRepository);
        orderedRepository.verify(supportRepository).increaseViewCount(supportId);
        orderedRepository.verify(supportRepository).findById(supportId);
    }

    @Test
    @DisplayName("존재하지 않는 지원정보 상세 조회 시 SUPPORT404 예외 발생")
    void getSupport_Fail_WhenSupportNotFound() {
        // given
        Long supportId = 999L;
        when(supportRepository.increaseViewCount(supportId)).thenReturn(0);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> supportService.getSupport(USER_ID, supportId));

        assertEquals(
                ErrorCode.SUPPORT_NOT_FOUND,
                exception.getErrorCode());
        verify(supportRepository, never()).findById(supportId);
    }

    @Test
    @DisplayName("지원정보 목록 인기순 조회 성공")
    void getSupports_Success_OrderByPopular() throws Exception {
        // given
        SupportInfo firstSupport = createSupport(3L, "첫 번째 공고", 100);
        SupportInfo secondSupport = createSupport(2L, "두 번째 공고", 80);
        SupportInfo nextSupport = createSupport(1L, "다음 페이지 공고", 50);

        when(supportRepository.findAllByPopular(
                null, null, PageRequest.of(0, 3)))
                .thenReturn(List.of(firstSupport, secondSupport, nextSupport));
        when(bookmarkRepository.findBookmarkedSupportIds(
                USER_ID, List.of(3L, 2L)))
                .thenReturn(List.of(3L));

        // when
        SupportResDTO.SupportListDTO result = supportService.getSupports(
                USER_ID, "POPULAR", null, "2");

        // then
        assertEquals(2, result.supports().size());
        assertEquals(3L, result.supports().get(0).supportId());
        assertEquals("첫 번째 공고", result.supports().get(0).title());
        assertTrue(result.supports().get(0).isBookmarked());
        assertFalse(result.supports().get(1).isBookmarked());
        assertTrue(result.page().hasNext());
        assertEquals("80_2", result.page().nextCursor());
    }

    @Test
    @DisplayName("지원정보 목록의 다음 페이지가 없으면 커서를 반환하지 않음")
    void getSupports_Success_WithoutNextPage() throws Exception {
        // given
        SupportInfo support = createSupport(1L, "지원 공고", 10);

        when(supportRepository.findAllByPopular(
                null, null, PageRequest.of(0, 21)))
                .thenReturn(List.of(support));
        when(bookmarkRepository.findBookmarkedSupportIds(
                USER_ID, List.of(1L)))
                .thenReturn(List.of());

        // when
        SupportResDTO.SupportListDTO result = supportService.getSupports(
                USER_ID, "POPULAR", null, "20");

        // then
        assertEquals(1, result.supports().size());
        assertFalse(result.page().hasNext());
        assertNull(result.page().nextCursor());
    }

    @Test
    @DisplayName("인기순 커서를 기준으로 다음 지원정보 목록 조회")
    void getSupports_Success_WithPopularCursor() {
        // given
        when(supportRepository.findAllByPopular(
                1520, 1L, PageRequest.of(0, 21)))
                .thenReturn(List.of());

        // when
        supportService.getSupports(
                USER_ID, "POPULAR", "1520_1", "20");

        // then
        verify(supportRepository).findAllByPopular(
                1520, 1L, PageRequest.of(0, 21));
    }

    @Test
    @DisplayName("지원하지 않는 정렬값이면 SUPPORT400 예외 발생")
    void getSupports_Fail_WhenSortIsInvalid() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> supportService.getSupports(
                        USER_ID, "OLDEST", null, "20"));

        assertEquals(
                ErrorCode.SUPPORT_INVALID_QUERY,
                exception.getErrorCode());
        assertEquals("SUPPORT400", exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("커서 형식이 잘못되면 SUPPORT400 예외 발생")
    void getSupports_Fail_WhenCursorIsInvalid() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> supportService.getSupports(
                        USER_ID, "POPULAR", "invalid", "20"));

        assertEquals(
                ErrorCode.SUPPORT_INVALID_QUERY,
                exception.getErrorCode());
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 SUPPORT400 예외 발생")
    void getSupports_Fail_WhenSizeIsOutOfRange() {
        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> supportService.getSupports(
                        USER_ID, "POPULAR", null, "101"));

        assertEquals(
                ErrorCode.SUPPORT_INVALID_QUERY,
                exception.getErrorCode());
    }

    private SupportInfo createSupport(Long id, String title, int viewCount) throws Exception {
        SupportInfo supportInfo = SupportInfo.builder()
                .organizationName("소상공인시장진흥공단")
                .title(title)
                .applyStartDate(LocalDate.of(2026, 1, 1))
                .applyEndDate(LocalDate.of(2026, 12, 31))
                .status(SupportStatus.ONGOING)
                .build();

        setField(supportInfo, "id", id);
        setField(supportInfo, "viewCount", viewCount);
        return supportInfo;
    }

    private void setField(SupportInfo supportInfo, String name, Object value) throws Exception {
        Field field = SupportInfo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(supportInfo, value);
    }
}
