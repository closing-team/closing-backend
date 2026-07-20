package com.closing.closing.domain.terms.controller;

import com.closing.closing.domain.terms.dto.request.AgreeTermsRequest;
import com.closing.closing.domain.terms.dto.response.TermResponse;
import com.closing.closing.domain.terms.service.TermsService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Terms", description = "약관 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "약관 목록 조회")
    @GetMapping
    public ApiResponse<List<TermResponse>> getTerms() {
        return ApiResponse.onSuccess(termsService.getLatestTerms());
    }

    @Operation(summary = "약관 동의")
    @PostMapping("/agree")
    public ApiResponse<Void> agreeTerms(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody AgreeTermsRequest request) {
        termsService.agreeTerms(authorizationHeader, request);
        return ApiResponse.onSuccess(null);
    }
}
