package com.closing.closing.domain.auth.controller;

import com.closing.closing.domain.auth.dto.request.KakaoLoginRequest;
import com.closing.closing.domain.auth.dto.request.SignupRequest;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.auth.dto.response.SignupResponse;
import com.closing.closing.domain.auth.dto.response.LoginResponse;
import com.closing.closing.domain.auth.dto.response.SignupResponse;
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

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody SignupRequest request) {
        return ApiResponse.onSuccess(authService.signup(authorizationHeader, request));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ApiResponse.onSuccess(null);
    }
}
