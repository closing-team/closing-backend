package com.closing.closing.domain.user.controller;

import com.closing.closing.domain.user.service.UserService;
import com.closing.closing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(
            @RequestHeader("Authorization") String authorizationHeader) {
        userService.withdraw(authorizationHeader);
        return ApiResponse.onSuccess(null);
    }
}
