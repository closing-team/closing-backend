package com.closing.closing.domain.user.service;

import com.closing.closing.domain.user.dto.response.UserInfoResponse;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void withdraw(String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.withdraw();
    public UserInfoResponse updateMyInfo(String authorizationHeader, UpdateUserRequest request) {
        Long userId = extractUserId(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateInfo(request.getName(), request.getPhone());
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }

    private Long extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String token = authorizationHeader.substring(7);
        try {
            jwtProvider.validate(token);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (jwtProvider.isSignupToken(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return jwtProvider.getUserId(token);
    }
}
