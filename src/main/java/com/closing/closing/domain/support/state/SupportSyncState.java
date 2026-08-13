package com.closing.closing.domain.support.state;

import com.closing.closing.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SupportSyncState {

    private volatile SyncStatus latestStatus = SyncStatus.NOT_STARTED;
    private volatile boolean initialSyncCompleted;
    private volatile ErrorCode latestFailure;

    public void markStarted() {
        latestStatus = SyncStatus.SYNCING;
        latestFailure = null;
    }

    public void markSucceeded() {
        initialSyncCompleted = true;
        latestStatus = SyncStatus.SUCCEEDED;
        latestFailure = null;
    }

    public void markFailed(ErrorCode errorCode) {
        latestStatus = SyncStatus.FAILED;
        latestFailure = errorCode;
    }

    public boolean isInitialSyncCompleted() {
        return initialSyncCompleted;
    }

    public ErrorCode getLatestFailure() {
        return latestFailure;
    }

    public SyncStatus getLatestStatus() {
        return latestStatus;
    }

    public enum SyncStatus {
        NOT_STARTED,
        SYNCING,
        SUCCEEDED,
        FAILED
    }
}
