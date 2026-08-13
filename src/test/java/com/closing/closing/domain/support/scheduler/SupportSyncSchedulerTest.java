package com.closing.closing.domain.support.scheduler;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.closing.closing.domain.support.service.SupportSyncService;
import com.closing.closing.domain.support.state.SupportSyncState;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportSyncSchedulerTest {

    @Mock
    private SupportSyncService supportSyncService;

    @Mock
    private SupportSyncState supportSyncState;

    @InjectMocks
    private SupportSyncScheduler supportSyncScheduler;

    @Test
    @DisplayName("지원정보 동기화 성공 상태 기록")
    void syncClosureSupports_Success_MarkSucceeded() {
        // given
        when(supportSyncService.syncClosureSupports()).thenReturn(14);

        // when
        supportSyncScheduler.syncClosureSupports();

        // then
        var ordered = inOrder(supportSyncState, supportSyncService);
        ordered.verify(supportSyncState).markStarted();
        ordered.verify(supportSyncService).syncClosureSupports();
        ordered.verify(supportSyncState).markSucceeded();
    }

    @Test
    @DisplayName("외부 API 동기화 실패 상태 기록")
    void syncClosureSupports_Fail_MarkFailed() {
        // given
        when(supportSyncService.syncClosureSupports())
                .thenThrow(new CustomException(
                        ErrorCode.SUPPORT_EXTERNAL_API_ERROR));

        // when
        supportSyncScheduler.syncClosureSupports();

        // then
        var ordered = inOrder(supportSyncState, supportSyncService);
        ordered.verify(supportSyncState).markStarted();
        ordered.verify(supportSyncService).syncClosureSupports();
        ordered.verify(supportSyncState).markFailed(
                ErrorCode.SUPPORT_EXTERNAL_API_ERROR);
    }

    @Test
    @DisplayName("예상하지 못한 동기화 오류도 실패 상태 기록")
    void syncClosureSupports_Fail_WhenUnexpectedException() {
        // given
        when(supportSyncService.syncClosureSupports())
                .thenThrow(new IllegalStateException("unexpected"));

        // when
        supportSyncScheduler.syncClosureSupports();

        // then
        Mockito.verify(supportSyncState).markFailed(
                ErrorCode.SUPPORT_EXTERNAL_API_ERROR);
    }
}
