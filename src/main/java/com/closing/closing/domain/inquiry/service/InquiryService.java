package com.closing.closing.domain.inquiry.service;

import com.closing.closing.domain.inquiry.dto.request.CreateInquiryRequest;
import com.closing.closing.domain.inquiry.dto.response.InquiryResponse;
import com.closing.closing.domain.inquiry.entity.Inquiry;
import com.closing.closing.domain.inquiry.repository.InquiryRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse createInquiry(CreateInquiryRequest request) {
        User user = getCurrentUser();
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .type(request.getType())
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
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

    private User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
