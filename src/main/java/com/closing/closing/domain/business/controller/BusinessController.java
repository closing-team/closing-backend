package com.closing.closing.domain.business.controller;

import com.closing.closing.domain.business.dto.request.VerifyBusinessRequest;
import com.closing.closing.domain.business.dto.response.BusinessVerifyResponse;
import com.closing.closing.domain.business.service.BusinessService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Business", description = "사업자 인증 API")
@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BusinessController {

    private final BusinessService businessService;

    @Operation(summary = "사업자 인증 / 재인증")
    @PutMapping("/verify")
    public ApiResponse<BusinessVerifyResponse> verify(@Valid @RequestBody VerifyBusinessRequest request) {
        return ApiResponse.onSuccess("사업자 인증이 완료되었습니다.", businessService.verify(request));
    }
}
