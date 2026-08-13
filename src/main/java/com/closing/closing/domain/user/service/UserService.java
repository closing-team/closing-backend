package com.closing.closing.domain.user.service;

import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.closing.closing.domain.business.repository.BusinessRegistrationRepository;
import com.closing.closing.domain.user.dto.response.UserInfoResponse;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BusinessRegistrationRepository businessRegistrationRepository;
    private final ImageStorage imageStorage;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo() {
        User user = getCurrentUser();
        BusinessRegistration business = businessRegistrationRepository.findByUserId(user.getId()).orElse(null);
        return UserInfoResponse.from(user, business);
    }

    @Transactional
    public UserInfoResponse updateMyInfo(String nickname, MultipartFile image) {
        User user = getCurrentUser();

        String profileImageUrl = user.getProfileImageUrl();
        if (image != null && !image.isEmpty()) {
            profileImageUrl = imageStorage.upload(image, "profiles");
        }

        user.updateProfile(nickname, profileImageUrl);

        BusinessRegistration business = businessRegistrationRepository.findByUserId(user.getId()).orElse(null);
        return UserInfoResponse.from(user, business);
    }

    @Transactional
    public void withdraw() {
        User user = getCurrentUser();
        user.withdraw();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }
}
