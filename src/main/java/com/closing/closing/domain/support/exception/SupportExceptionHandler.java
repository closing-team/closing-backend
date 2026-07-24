package com.closing.closing.domain.support.exception;

import com.closing.closing.domain.support.exception.code.SupportErrorCode;
import com.closing.closing.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SupportExceptionHandler {

    @ExceptionHandler(SupportException.class)
    public ResponseEntity<ApiResponse<Void>> handleSupportException(SupportException exception) {
        SupportErrorCode errorCode = exception.getSupportErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
