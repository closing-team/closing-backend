package com.closing.closing.domain.auth.controller;

import com.closing.closing.domain.auth.dto.request.KakaoLoginRequest;
import com.closing.closing.domain.auth.dto.request.SignupRequest;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.auth.dto.response.SignupResponse;
import com.closing.closing.domain.auth.dto.response.TokenRefreshResponse;
import com.closing.closing.domain.auth.service.AuthService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. Auth", description = "인증 API")
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

    @Operation(summary = "회원가입")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            HttpServletRequest httpRequest,
            @Valid @RequestBody SignupRequest request) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        return ApiResponse.onSuccess(authService.signup(authorizationHeader, request));
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 재발급합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(HttpServletRequest httpRequest) {
        return ApiResponse.onSuccess(authService.refresh(httpRequest.getHeader("Authorization")));
    }

    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest.getHeader("Authorization"));
        return ApiResponse.onSuccess(null);
    }
}
