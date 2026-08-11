package com.closing.closing.domain.auth.controller;

import com.closing.closing.domain.auth.dto.request.KakaoLoginRequest;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 소셜 로그인")
    @PostMapping("/kakao")
    public ApiResponse<LoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.onSuccess(authService.kakaoLogin(request.getCode()));
    }

    @Operation(summary = "회원가입", description = "텍스트 필드는 form-data로, 프로필 이미지는 image 파트로 전송합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SignupResponse> signup(
            HttpServletRequest httpRequest,
            @NotBlank(message = "이름을 입력해주세요.") @RequestParam("name") String name,
            @NotBlank(message = "닉네임을 입력해주세요.") @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.") @RequestParam("nickname") String nickname,
            @NotBlank(message = "전화번호를 입력해주세요.") @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.") @RequestParam("phone") String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        String authorizationHeader = httpRequest.getHeader("Authorization");
        return ApiResponse.onSuccess(authService.signup(authorizationHeader, name, nickname, phone, email, image));
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
