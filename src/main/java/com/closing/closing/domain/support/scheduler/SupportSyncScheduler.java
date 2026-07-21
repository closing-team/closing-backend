package com.closing.closing.domain.support.scheduler;

import com.closing.closing.domain.support.exception.SupportException;
import com.closing.closing.domain.support.service.SupportSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportSyncScheduler {

    private final SupportSyncService supportSyncService;

    @Scheduled(
            initialDelayString = "${BIZINFO_SYNC_INITIAL_DELAY:10000}",
            fixedDelayString = "${BIZINFO_SYNC_INTERVAL:86400000}")
    public void syncClosureSupports() {
        try {
            int syncedCount = supportSyncService.syncClosureSupports();
            log.info("기업마당 폐업지원 공고 동기화 완료: {}건", syncedCount);
        } catch (SupportException exception) {
            log.warn("기업마당 폐업지원 공고 동기화 실패: {}",
                    exception.getSupportErrorCode().name());
        }
    }
}
