package com.closing.closing.domain.support.service;

import com.closing.closing.domain.support.client.BizInfoClient;
import com.closing.closing.domain.support.dto.BizInfoResDTO;
import com.closing.closing.domain.support.entity.SupportInfo;
import com.closing.closing.domain.support.entity.SupportStatus;
import com.closing.closing.domain.support.repository.SupportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportSyncServiceTest {

    @Mock
    private BizInfoClient bizInfoClient;

    @Mock
    private SupportRepository supportRepository;

    @InjectMocks
    private SupportSyncService supportSyncService;

    @BeforeEach
    void setUp() {
        lenient().when(supportRepository
                .findAllByExternalIdIsNotNullOrApplicationUrlStartingWith(
                        "https://www.bizinfo.go.kr/"))
                .thenReturn(List.of());
    }

    @Test
    @DisplayName("기업마당 공고 중 폐업지원 공고만 저장")
    void syncClosureSupports_Success_SaveClosureSupportOnly() {
        // given
        BizInfoResDTO.BizInfoItemDTO closureSupport = createItem(
                "PBLN_1",
                "2026년 희망리턴패키지 원스톱 폐업지원",
                "<p>소상공인의 사업 정리를 지원합니다.</p>",
                "20260101 ~ 20991231",
                "폐업,희망리턴",
                "https://www.bizinfo.go.kr/support/1",
                1520,
                2);
        BizInfoResDTO.BizInfoItemDTO normalSupport = createItem(
                "PBLN_2",
                "중소기업 기술개발 지원사업",
                "기술개발 비용을 지원합니다.",
                "2026-01-01 ~ 2099-12-31",
                "기술,중소기업",
                "https://www.bizinfo.go.kr/support/2",
                10,
                2);

        when(bizInfoClient.getSupportAnnouncements(100, 1))
                .thenReturn(new BizInfoResDTO.BizInfoResponseDTO(
                        List.of(closureSupport, normalSupport)));
        when(supportRepository.findByExternalId(closureSupport.pblancId()))
                .thenReturn(Optional.empty());
        when(supportRepository.findLegacyByAnnouncementUrl(closureSupport.pblancUrl()))
                .thenReturn(Optional.empty());

        // when
        int syncedCount = supportSyncService.syncClosureSupports();

        // then
        ArgumentCaptor<SupportInfo> captor = ArgumentCaptor.forClass(SupportInfo.class);
        verify(supportRepository).save(captor.capture());
        verify(supportRepository, never())
                .findLegacyByAnnouncementUrl(normalSupport.pblancUrl());

        SupportInfo savedSupport = captor.getValue();
        assertEquals(1, syncedCount);
        assertEquals("중소벤처기업부", savedSupport.getOrganizationName());
        assertEquals(closureSupport.pblancNm(), savedSupport.getTitle());
        assertEquals("""
                소상공인의 사업 정리를 지원합니다.

                지원대상
                소상공인

                [사업신청 방법]
                온라인 접수

                [문의처]
                중소기업통합콜센터""", savedSupport.getContent());
        assertEquals(LocalDate.of(2026, 1, 1), savedSupport.getApplyStartDate());
        assertEquals(LocalDate.of(2099, 12, 31), savedSupport.getApplyEndDate());
        assertEquals(SupportStatus.ONGOING, savedSupport.getStatus());
        assertEquals(1520, savedSupport.getViewCount());
        assertEquals("PBLN_1", savedSupport.getExternalId());
        assertEquals("https://apply.example.com", savedSupport.getApplicationUrl());
    }

    @Test
    @DisplayName("이미 저장된 기업마당 공고는 최신 정보로 갱신")
    void syncClosureSupports_Success_UpdateExistingSupport() throws Exception {
        // given
        String externalUrl = "https://www.bizinfo.go.kr/support/1";
        SupportInfo existingSupport = SupportInfo.builder()
                .organizationName("기존 기관")
                .title("기존 제목")
                .content("기존 내용")
                .applicationUrl(externalUrl)
                .status(SupportStatus.ONGOING)
                .viewCount(0)
                .build();
        setField(existingSupport, "id", 1L);
        BizInfoResDTO.BizInfoItemDTO item = createItem(
                "PBLN_1",
                "폐업지원 공고 수정본",
                null,
                "2020-01-01 ~ 2020-12-31",
                "폐업",
                externalUrl,
                100,
                1);

        when(bizInfoClient.getSupportAnnouncements(100, 1))
                .thenReturn(new BizInfoResDTO.BizInfoResponseDTO(List.of(item)));
        when(supportRepository.findByExternalId(item.pblancId()))
                .thenReturn(Optional.of(existingSupport));

        // when
        int syncedCount = supportSyncService.syncClosureSupports();

        // then
        assertEquals(1, syncedCount);
        verify(supportRepository).updateFromExternal(
                1L,
                "중소벤처기업부",
                "폐업지원 공고 수정본",
                """
                지원대상
                소상공인

                [사업신청 방법]
                온라인 접수

                [문의처]
                중소기업통합콜센터""",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 12, 31),
                null,
                "PBLN_1",
                "https://apply.example.com",
                SupportStatus.CLOSED,
                100);
        verify(supportRepository, never())
                .findLegacyByAnnouncementUrl(item.pblancUrl());
        verify(supportRepository, never()).save(existingSupport);
    }

    @Test
    @DisplayName("구 응답 필드 reqstDt의 신청기간도 추출")
    void syncClosureSupports_Success_ParseLegacyApplicationPeriod() {
        // given
        String externalUrl = "https://www.bizinfo.go.kr/support/legacy";
        BizInfoResDTO.BizInfoItemDTO legacyItem = new BizInfoResDTO.BizInfoItemDTO(
                null,
                null,
                null,
                null,
                "소상공인시장진흥공단",
                null,
                null,
                "소상공인",
                null,
                null,
                null,
                43,
                1,
                "폐업",
                "PBLN_OLD",
                "폐업지원 공고",
                externalUrl,
                "중소벤처기업부",
                "폐업 소상공인을 지원합니다.",
                "20220727 ~ 20220930",
                null,
                null);

        when(bizInfoClient.getSupportAnnouncements(100, 1))
                .thenReturn(new BizInfoResDTO.BizInfoResponseDTO(List.of(legacyItem)));
        when(supportRepository.findByExternalId(legacyItem.announcementId()))
                .thenReturn(Optional.empty());
        when(supportRepository.findLegacyByAnnouncementUrl(externalUrl))
                .thenReturn(Optional.empty());

        // when
        supportSyncService.syncClosureSupports();

        // then
        ArgumentCaptor<SupportInfo> captor = ArgumentCaptor.forClass(SupportInfo.class);
        verify(supportRepository).save(captor.capture());
        assertEquals(LocalDate.of(2022, 7, 27), captor.getValue().getApplyStartDate());
        assertEquals(LocalDate.of(2022, 9, 30), captor.getValue().getApplyEndDate());
    }

    @Test
    @DisplayName("문자 신청기간은 등록일과 기간 원문으로 저장")
    void syncClosureSupports_Success_SaveTextApplicationPeriod() {
        // given
        String externalUrl = "https://www.bizinfo.go.kr/support/text-period";
        BizInfoResDTO.BizInfoItemDTO item = new BizInfoResDTO.BizInfoItemDTO(
                "PBLN_TEXT",
                "[전북] 폐업 소상공인 사업정리 지원사업 공고",
                externalUrl,
                "전북특별자치도",
                null,
                "폐업 소상공인의 사업정리를 지원합니다.",
                "예산 소진시까지",
                "소상공인",
                null,
                null,
                null,
                100,
                1,
                "폐업,사업정리",
                null,
                null,
                null,
                null,
                null,
                null,
                "2026-03-05 10:30:00",
                null);

        when(bizInfoClient.getSupportAnnouncements(100, 1))
                .thenReturn(new BizInfoResDTO.BizInfoResponseDTO(List.of(item)));
        when(supportRepository.findByExternalId(item.pblancId()))
                .thenReturn(Optional.empty());
        when(supportRepository.findLegacyByAnnouncementUrl(externalUrl))
                .thenReturn(Optional.empty());

        // when
        supportSyncService.syncClosureSupports();

        // then
        ArgumentCaptor<SupportInfo> captor = ArgumentCaptor.forClass(SupportInfo.class);
        verify(supportRepository).save(captor.capture());
        assertEquals(LocalDate.of(2026, 3, 5), captor.getValue().getApplyStartDate());
        assertNull(captor.getValue().getApplyEndDate());
        assertEquals("예산 소진시까지", captor.getValue().getApplicationPeriod());
    }

    @Test
    @DisplayName("본문에만 폐업 문구가 있는 비관련 공고는 기존 목록에서 제거")
    void syncClosureSupports_Success_DeleteNonClosureSupport() {
        // given
        SupportInfo closureSupport = SupportInfo.builder()
                .organizationName("전북특별자치도")
                .title("폐업 소상공인 사업정리 지원사업")
                .content("폐업을 지원합니다.")
                .applicationUrl("https://www.bizinfo.go.kr/support/closure")
                .status(SupportStatus.ONGOING)
                .build();
        SupportInfo unrelatedSupport = SupportInfo.builder()
                .organizationName("중소벤처기업부")
                .title("중소기업 R&D 지원사업")
                .content("신청일 기준 폐업 상태가 아닌 기업")
                .applicationUrl("https://www.bizinfo.go.kr/support/unrelated")
                .status(SupportStatus.ONGOING)
                .build();

        when(bizInfoClient.getSupportAnnouncements(100, 1))
                .thenReturn(new BizInfoResDTO.BizInfoResponseDTO(List.of()));
        when(supportRepository
                .findAllByExternalIdIsNotNullOrApplicationUrlStartingWith(
                        "https://www.bizinfo.go.kr/"))
                .thenReturn(List.of(closureSupport, unrelatedSupport));

        // when
        supportSyncService.syncClosureSupports();

        // then
        verify(supportRepository).deleteAll(List.of(unrelatedSupport));
    }

    private BizInfoResDTO.BizInfoItemDTO createItem(
            String id,
            String title,
            String summary,
            String applicationPeriod,
            String hashtags,
            String externalUrl,
            int viewCount,
            int totalCount) {
        return new BizInfoResDTO.BizInfoItemDTO(
                id,
                title,
                externalUrl,
                "중소벤처기업부",
                "소상공인시장진흥공단",
                summary,
                applicationPeriod,
                "소상공인",
                "온라인 접수",
                "중소기업통합콜센터",
                "https://apply.example.com",
                viewCount,
                totalCount,
                hashtags,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private void setField(SupportInfo supportInfo, String name, Object value)
            throws Exception {
        Field field = SupportInfo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(supportInfo, value);
    }
}
