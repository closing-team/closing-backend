package com.closing.closing.domain.support.enums;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;

public enum SupportSort {
    POPULAR,
    LATEST,
    DEADLINE;

    public static SupportSort from(String value) {
        try {
            return SupportSort.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new CustomException(ErrorCode.SUPPORT_INVALID_QUERY);
        }
    }
}
