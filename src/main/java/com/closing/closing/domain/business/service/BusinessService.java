package com.closing.closing.domain.business.service;

import com.closing.closing.domain.business.client.NtsClient;
import com.closing.closing.domain.business.client.dto.NtsValidateResponse;
import com.closing.closing.domain.business.dto.request.VerifyBusinessRequest;
import com.closing.closing.domain.business.dto.response.BusinessVerifyResponse;
import com.closing.closing.domain.business.entity.BusinessRegistration;
import com.closing.closing.domain.business.repository.BusinessRegistrationRepository;
import com.closing.closing.domain.user.entity.User;
import com.closing.closing.domain.user.repository.UserRepository;
import com.closing.closing.global.exception.CustomException;
import com.closing.closing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private static final DateTimeFormatter NTS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final NtsClient ntsClient;
    private final BusinessRegistrationRepository businessRegistrationRepository;
    private final UserRepository userRepository;

    @Transactional
    public BusinessVerifyResponse verify(VerifyBusinessRequest request) {
        NtsValidateResponse response = ntsClient.validate(
                request.getBusinessNumber(), request.getOpenDate(), request.getOwnerName());

        NtsValidateResponse.BusinessData result = extractResult(response);

        if (!"01".equals(result.getValid())) {
            throw new CustomException(ErrorCode.BUSINESS_MISMATCH);
        }

        LocalDateTime closeDate = parseCloseDate(result.getStatus());

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime openDate = LocalDate.parse(request.getOpenDate(), NTS_DATE_FORMAT).atStartOfDay();

        Optional<BusinessRegistration> existing = businessRegistrationRepository.findByUserId(userId);
        BusinessRegistration registration;

        if (existing.isPresent()) {
            existing.get().update(request.getBusinessNumber(), request.getOwnerName(), openDate, closeDate);
            registration = existing.get();
        } else {
            registration = businessRegistrationRepository.save(
                    BusinessRegistration.builder()
                            .user(user)
                            .businessNumber(request.getBusinessNumber())
                            .businessOwnerName(request.getOwnerName())
                            .businessOpenDate(openDate)
                            .businessCloseDate(closeDate)
                            .build()
            );
        }

        return BusinessVerifyResponse.from(registration);
    }

    private NtsValidateResponse.BusinessData extractResult(NtsValidateResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new CustomException(ErrorCode.BUSINESS_NTS_FAIL);
        }
        return response.getData().get(0);
    }

    private LocalDateTime parseCloseDate(NtsValidateResponse.NtsStatus status) {
        if (status == null) return null;
        String endDt = status.getEndDt();
        if (endDt == null || endDt.isBlank()) return null;

        LocalDate closeDate = LocalDate.parse(endDt, NTS_DATE_FORMAT);
        if (LocalDate.now().isAfter(closeDate.plusMonths(6))) {
            throw new CustomException(ErrorCode.BUSINESS_CLOSED_EXPIRED);
        }
        return closeDate.atStartOfDay();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
