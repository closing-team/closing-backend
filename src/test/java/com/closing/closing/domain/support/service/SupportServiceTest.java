package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import com.closing.closing.domain.support.repository.SupportRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportRepository supportRepository;

    @InjectMocks
    private SupportService supportService;

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

        // when
        SupportResDTO.SupportListDTO result = supportService.getSupports(
                "POPULAR", null, "2", null);

        // then
        assertEquals(2, result.supports().size());
        assertEquals(3L, result.supports().get(0).supportId());
        assertEquals("첫 번째 공고", result.supports().get(0).title());
        assertFalse(result.supports().get(0).isBookmarked());
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

        // when
        SupportResDTO.SupportListDTO result = supportService.getSupports(
                "POPULAR", null, "20", null);

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
        supportService.getSupports("POPULAR", "1520_1", "20", null);

        // then
        verify(supportRepository).findAllByPopular(
                1520, 1L, PageRequest.of(0, 21));
    }

    @Test
    @DisplayName("지원하지 않는 정렬값이면 COMMON400 예외 발생")
    void getSupports_Fail_WhenSortIsInvalid() {
        // when & then
        SupportException exception = assertThrows(SupportException.class,
                () -> supportService.getSupports("OLDEST", null, "20", null));

        assertEquals(
                SupportErrorCode.SUPPORT_INVALID_QUERY,
                exception.getSupportErrorCode());
    }

    @Test
    @DisplayName("커서 형식이 잘못되면 COMMON400 예외 발생")
    void getSupports_Fail_WhenCursorIsInvalid() {
        // when & then
        SupportException exception = assertThrows(SupportException.class,
                () -> supportService.getSupports("POPULAR", "invalid", "20", null));

        assertEquals(
                SupportErrorCode.SUPPORT_INVALID_QUERY,
                exception.getSupportErrorCode());
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 COMMON400 예외 발생")
    void getSupports_Fail_WhenSizeIsOutOfRange() {
        // when & then
        SupportException exception = assertThrows(SupportException.class,
                () -> supportService.getSupports("POPULAR", null, "101", null));

        assertEquals(
                SupportErrorCode.SUPPORT_INVALID_QUERY,
                exception.getSupportErrorCode());
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
