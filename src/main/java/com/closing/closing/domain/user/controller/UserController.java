package com.closing.closing.domain.user.controller;

import com.closing.closing.domain.user.dto.response.UserInfoResponse;
import com.closing.closing.domain.user.service.UserService;
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

@Tag(name = "03. User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMyInfo() {
        return ApiResponse.onSuccess(userService.getMyInfo());
    }

    @Operation(summary = "내 정보 수정 (닉네임·프로필 이미지)", description = "닉네임과 프로필 이미지를 수정합니다. 이미지 변경이 없을 경우 image 파트를 생략하세요.")
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserInfoResponse> updateMyInfo(
            @NotBlank(message = "닉네임을 입력해주세요.") @RequestParam("nickname") String nickname,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.onSuccess(userService.updateMyInfo(nickname, image));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw() {
        userService.withdraw();
        return ApiResponse.onSuccess(null);
    }
}
