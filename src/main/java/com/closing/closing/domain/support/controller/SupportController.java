package com.closing.closing.domain.support.controller;

import com.closing.closing.domain.support.dto.SupportResDTO;
import com.closing.closing.domain.support.service.SupportService;
import com.closing.closing.domain.support.enums.SupportSort;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "07. Support", description = "지원정보 API")
@RestController
@RequestMapping("/api/v1/supports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final SupportService supportService;

    @Operation(summary = "지원정보 목록 조회", description = "지원정보 목록을 커서 기반으로 조회합니다.")
    @GetMapping
    public ApiResponse<SupportResDTO.SupportListDTO> getSupports(
            @AuthenticationPrincipal Long userId,
            @Parameter(
                    description = "지원정보 정렬 기준",
                    schema = @Schema(
                            implementation = SupportSort.class,
                            defaultValue = "POPULAR"))
            @RequestParam(value = "sort", defaultValue = "POPULAR") String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "20") String size
    ) {
        return ApiResponse.onSuccess(
                supportService.getSupports(userId, sort, cursor, size));
    }

    @Operation(summary = "지원정보 상세 조회", description = "지원정보의 상세 내용을 조회합니다.")
    @GetMapping("/{supportId}")
    public ApiResponse<SupportResDTO.SupportDetailDTO> getSupport(
            @AuthenticationPrincipal Long userId,
            @PathVariable("supportId") Long supportId
    ) {
        return ApiResponse.onSuccess(
                supportService.getSupport(userId, supportId));
    }
}
