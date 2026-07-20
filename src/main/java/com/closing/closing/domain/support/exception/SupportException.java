package com.closing.closing.domain.support.exception;

import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import lombok.Getter;

@Getter
public class SupportException extends RuntimeException {

    private final SupportErrorCode supportErrorCode;

    public SupportException(SupportErrorCode supportErrorCode) {
        super(supportErrorCode.getMessage());
        this.supportErrorCode = supportErrorCode;
    }
}
