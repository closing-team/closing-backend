package com.closing.closing.domain.inquiry.controller;

import com.closing.closing.domain.inquiry.dto.request.CreateInquiryRequest;
import com.closing.closing.domain.inquiry.dto.response.InquiryResponse;
import com.closing.closing.domain.inquiry.service.InquiryService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inquiry", description = "1:1 문의 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록")
    @PostMapping
    public ApiResponse<InquiryResponse> createInquiry(@Valid @RequestBody CreateInquiryRequest request) {
        return ApiResponse.onSuccess(inquiryService.createInquiry(request));
    }

    @Operation(summary = "내 문의 내역 조회")
    @GetMapping
    public ApiResponse<List<InquiryResponse>> getMyInquiries() {
        return ApiResponse.onSuccess(inquiryService.getMyInquiries());
    }
}
