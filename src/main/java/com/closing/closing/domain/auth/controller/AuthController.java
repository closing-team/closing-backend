package com.closing.closing.domain.auth.controller;

import com.closing.closing.domain.auth.dto.request.KakaoLoginRequest;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.auth.service.AuthService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 소셜 로그인")
    @PostMapping("/kakao")
    public ApiResponse<LoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.onSuccess(authService.kakaoLogin(request.getCode()));
    }
}
