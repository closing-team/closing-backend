package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.SupportService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Support", description = "지원정보 API")
@RestController
@RequestMapping("/api/v1/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    /**
     * 지원정보 목록 조회
     */
    @Operation(summary = "지원정보 목록 조회", description = "지원정보 목록을 커서 기반으로 조회합니다.")
    @GetMapping
    public ApiResponse<SupportResDTO.SupportListDTO> getSupports(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "sort", defaultValue = "POPULAR") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "20") String size
    ) {
        return ApiResponse.onSuccess(
                supportService.getSupports(sort, cursor, size, authorizationHeader));
    }
}
