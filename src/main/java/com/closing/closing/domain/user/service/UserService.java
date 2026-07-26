package com.closing.closing.domain.user.service;

import com.closing.closing.domain.user.dto.request.UpdateUserRequest;
import com.closing.closing.domain.user.dto.response.UserInfoResponse;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo() {
        User user = getCurrentUser();
        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateMyInfo(UpdateUserRequest request) {
        User user = getCurrentUser();
        user.updateInfo(request.getName(), request.getPhone());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void withdraw() {
        User user = getCurrentUser();
        user.withdraw();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
