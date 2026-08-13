package com.closing.closing.domain.support.scheduler;

import com.closing.closing.domain.support.service.SupportSyncService;
import com.closing.closing.domain.support.state.SupportSyncState;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportSyncScheduler {

    private final SupportSyncService supportSyncService;
    private final SupportSyncState supportSyncState;

    @Scheduled(
            initialDelayString = "${BIZINFO_SYNC_INITIAL_DELAY:0}",
            fixedDelayString = "${BIZINFO_SYNC_INTERVAL:86400000}")
    public void syncClosureSupports() {
        long startedAt = System.nanoTime();
        supportSyncState.markStarted();
        log.info("기업마당 폐업지원 공고 동기화 시작");

        try {
            int syncedCount = supportSyncService.syncClosureSupports();
            supportSyncState.markSucceeded();
            log.info(
                    "기업마당 폐업지원 공고 동기화 완료: {}건, 소요 시간 {}ms",
                    syncedCount,
                    elapsedMillis(startedAt));
        } catch (CustomException exception) {
            supportSyncState.markFailed(exception.getErrorCode());
            log.warn(
                    "기업마당 폐업지원 공고 동기화 실패: {}, 소요 시간 {}ms",
                    exception.getErrorCode().name(),
                    elapsedMillis(startedAt));
        } catch (RuntimeException exception) {
            supportSyncState.markFailed(ErrorCode.SUPPORT_EXTERNAL_API_ERROR);
            log.error(
                    "기업마당 폐업지원 공고 동기화 중 예상하지 못한 오류 발생, 소요 시간 {}ms",
                    elapsedMillis(startedAt),
                    exception);
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
