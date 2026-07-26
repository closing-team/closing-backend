package com.closing.closing.domain.user.controller;

import com.closing.closing.domain.user.dto.request.UpdateUserRequest;
import com.closing.closing.domain.user.dto.response.UserInfoResponse;
import com.closing.closing.domain.user.service.UserService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMyInfo() {
        return ApiResponse.onSuccess(userService.getMyInfo());
    }

    @Operation(summary = "내 정보 수정 (이름·전화번호)")
    @PatchMapping("/me")
    public ApiResponse<UserInfoResponse> updateMyInfo(@Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.onSuccess(userService.updateMyInfo(request));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw() {
        userService.withdraw();
        return ApiResponse.onSuccess(null);
    }
}
