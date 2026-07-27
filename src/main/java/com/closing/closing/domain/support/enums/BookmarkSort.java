package com.closing.closing.domain.support.enums;

import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;

public enum BookmarkSort {
    POPULAR,
    LATEST,
    DEADLINE;

    public static BookmarkSort from(String value) {
        try {
            return BookmarkSort.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new CustomException(ErrorCode.BOOKMARK_INVALID_QUERY);
        }
    }
}
