package com.closing.closing.domain.support.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.closing.closing.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SupportSyncStateTest {

    @Test
    @DisplayName("최초 동기화 성공 이력은 이후 동기화 실패에도 유지")
    void preserveInitialSyncCompletionAfterLaterFailure() {
        SupportSyncState state = new SupportSyncState();

        state.markStarted();
        assertFalse(state.isInitialSyncCompleted());

        state.markSucceeded();
        assertTrue(state.isInitialSyncCompleted());

        state.markStarted();
        state.markFailed(ErrorCode.SUPPORT_EXTERNAL_API_ERROR);

        assertTrue(state.isInitialSyncCompleted());
        assertEquals(
                ErrorCode.SUPPORT_EXTERNAL_API_ERROR,
                state.getLatestFailure());
    }
}
