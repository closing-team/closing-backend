package com.closing.closing.domain.inquiry.service;

import com.closing.closing.domain.inquiry.dto.response.InquiryResponse;
import com.closing.closing.domain.inquiry.entity.Inquiry;
import com.closing.closing.domain.inquiry.repository.InquiryRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import com.closing.closing.global.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final String INQUIRY_DIRECTORY = "inquiries";

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public InquiryResponse createInquiry(String type, String content, List<MultipartFile> images) {
        User user = getCurrentUser();

        List<String> imageUrls = uploadImages(images);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .type(type)
                .content(content)
                .imageUrls(imageUrls.isEmpty() ? null : imageUrls)
                .build();

        return InquiryResponse.from(inquiryRepository.save(inquiry));
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getMyInquiries() {
        Long userId = getCurrentUserId();
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InquiryResponse::from)
                .toList();
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return List.of();

        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                uploadedUrls.add(imageStorage.upload(image, INQUIRY_DIRECTORY));
            }
        } catch (RuntimeException e) {
            uploadedUrls.forEach(url -> {
                try { imageStorage.delete(url); } catch (Exception ex) {
                    log.warn("문의 이미지 정리 실패. url={}", url, ex);
                }
            });
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        return uploadedUrls;
    }

    private User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
