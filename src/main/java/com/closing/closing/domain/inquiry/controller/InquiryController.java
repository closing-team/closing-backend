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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Inquiry", description = "1:1 문의 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록 (이미지 첨부 선택)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResponse> createInquiry(
            @Valid @RequestPart("request") CreateInquiryRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.onSuccess(inquiryService.createInquiry(request, images));
    }

    @Operation(summary = "내 문의 내역 조회")
    @GetMapping
    public ApiResponse<List<InquiryResponse>> getMyInquiries() {
        return ApiResponse.onSuccess(inquiryService.getMyInquiries());
    }
}
