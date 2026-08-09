package com.closing.closing.domain.inquiry.controller;

import com.closing.closing.domain.inquiry.dto.response.InquiryResponse;
import com.closing.closing.domain.inquiry.service.InquiryService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Inquiry", description = "1:1 문의 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Validated
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록", description = "문의 유형과 내용을 전송합니다. 이미지는 images 파트로 여러 장 첨부 가능합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResponse> createInquiry(
            @NotBlank(message = "문의 유형을 입력해주세요.") @RequestParam("type") String type,
            @NotBlank(message = "문의 내용을 입력해주세요.") @RequestParam("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.onSuccess(inquiryService.createInquiry(type, content, images));
    }

    @Operation(summary = "내 문의 내역 조회")
    @GetMapping
    public ApiResponse<List<InquiryResponse>> getMyInquiries() {
        return ApiResponse.onSuccess(inquiryService.getMyInquiries());
    }
}
